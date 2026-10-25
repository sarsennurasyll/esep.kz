package com.esep.moneyflow.service;

import com.esep.moneyflow.interfaces.CounterpartyPurposeRuleCatalog;
import com.esep.moneyflow.interfaces.MoneyPurposeResolver;
import com.esep.moneyflow.interfaces.PurposeLabelingTransactions;
import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.CounterpartyActivity;
import com.esep.moneyflow.model.CounterpartyLabelingItem;
import com.esep.moneyflow.model.CounterpartyPurposeRule;
import com.esep.moneyflow.model.LabelingProgress;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.moneyflow.model.PurposeDecision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Личная разметка назначения операций по контрагенту: правило применяется ко всем операциям с этим
 * контрагентом в этом направлении и отменяется одним действием.
 */
@Service
public class PurposeLabelingService implements MoneyPurposeResolver {

    /** Типы движения, назначение которых по выписке неизвестно и может указать только пользователь. */
    public static final Set<MoneyFlowType> LABELABLE_FLOWS = Set.of(
            MoneyFlowType.TRANSFER_TO_PERSON, MoneyFlowType.TRANSFER_FROM_PERSON, MoneyFlowType.UNKNOWN
    );
    private static final int SHARE_SCALE = 4;

    private final CounterpartyPurposeRuleCatalog ruleCatalog;
    private final PurposeLabelingTransactions transactions;

    public PurposeLabelingService(CounterpartyPurposeRuleCatalog ruleCatalog, PurposeLabelingTransactions transactions) {
        this.ruleCatalog = ruleCatalog;
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public List<CounterpartyLabelingItem> findCounterparties() {
        Map<RuleKey, CounterpartyPurposeRule> rules = new LinkedHashMap<>();
        ruleCatalog.findAll().forEach(rule -> rules.put(new RuleKey(rule.counterpartyKey(), rule.direction()), rule));

        Map<RuleKey, Accumulator> items = new LinkedHashMap<>();
        for (CounterpartyActivity activity : transactions.findActivity(LABELABLE_FLOWS)) {
            RuleKey key = new RuleKey(CounterpartyKeys.of(activity.description()), activity.direction());
            items.computeIfAbsent(key, ignored -> new Accumulator(activity.description())).add(activity);
        }

        return items.entrySet().stream()
                .map(entry -> entry.getValue().toItem(entry.getKey(), rules.get(entry.getKey())))
                .sorted(Comparator.comparing(CounterpartyLabelingItem::amount).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public LabelingProgress progress() {
        List<CounterpartyLabelingItem> items = findCounterparties();
        BigDecimal total = items.stream().map(CounterpartyLabelingItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal labeled = items.stream().map(CounterpartyLabelingItem::labeledAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal share = total.signum() == 0 ? BigDecimal.ZERO.setScale(SHARE_SCALE) : labeled.divide(total, SHARE_SCALE, RoundingMode.HALF_UP);
        int rules = (int) items.stream().filter(item -> item.rule() != null).count();
        return new LabelingProgress(total, labeled, share, items.size(), rules);
    }

    @Transactional
    public CounterpartyPurposeRule setRule(String counterpartyKey, MoneyDirection direction, MoneyPurpose purpose) {
        String key = CounterpartyKeys.of(counterpartyKey);
        if (key.isBlank()) {
            throw new IllegalArgumentException("Counterparty must not be blank");
        }
        if (purpose == null || purpose == MoneyPurpose.UNKNOWN) {
            throw new IllegalArgumentException("Rule purpose must be known; remove the rule to return to unknown");
        }
        if (!direction.allows(purpose)) {
            throw new IllegalArgumentException("Purpose " + purpose + " is not allowed for " + direction);
        }

        CounterpartyPurposeRule rule = ruleCatalog.save(key, direction, purpose);
        transactions.applyRulePurpose(descriptionsOf(key, direction), direction, LABELABLE_FLOWS, purpose);
        return rule;
    }

    @Transactional
    public void removeRule(long ruleId) {
        CounterpartyPurposeRule rule = ruleCatalog.delete(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule was not found"));
        transactions.resetRulePurpose(descriptionsOf(rule.counterpartyKey(), rule.direction()), rule.direction(), LABELABLE_FLOWS);
    }

    /** Назначение для новой операции при импорте: по правилу пользователя или «неизвестно». */
    @Override
    public PurposeDecision decide(String description, BigDecimal amount, MoneyFlowType flowType) {
        if (!LABELABLE_FLOWS.contains(flowType)) {
            return PurposeDecision.unknown();
        }
        return ruleCatalog.find(CounterpartyKeys.of(description), MoneyDirection.of(amount))
                .map(rule -> new PurposeDecision(rule.purpose(), ClassificationSource.PERSONAL_RULE))
                .orElseGet(PurposeDecision::unknown);
    }

    private List<String> descriptionsOf(String key, MoneyDirection direction) {
        return transactions.findActivity(LABELABLE_FLOWS).stream()
                .filter(activity -> activity.direction() == direction && CounterpartyKeys.of(activity.description()).equals(key))
                .map(CounterpartyActivity::description)
                .distinct()
                .toList();
    }

    private record RuleKey(String counterpartyKey, MoneyDirection direction) {
    }

    private static final class Accumulator {
        private final String exampleDescription;
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal labeledAmount = BigDecimal.ZERO;
        private long operationCount;

        Accumulator(String exampleDescription) {
            this.exampleDescription = exampleDescription;
        }

        void add(CounterpartyActivity activity) {
            amount = amount.add(activity.amount());
            operationCount += activity.operationCount();
            if (activity.purpose() != MoneyPurpose.UNKNOWN) {
                labeledAmount = labeledAmount.add(activity.amount());
            }
        }

        CounterpartyLabelingItem toItem(RuleKey key, CounterpartyPurposeRule rule) {
            return new CounterpartyLabelingItem(key.counterpartyKey(), exampleDescription, key.direction(), amount, operationCount, labeledAmount, rule);
        }
    }
}
