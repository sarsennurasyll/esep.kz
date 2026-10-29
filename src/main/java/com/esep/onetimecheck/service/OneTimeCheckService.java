package com.esep.onetimecheck.service;

import com.esep.analytics.model.MonthlyMoneyFlow;
import com.esep.analytics.model.StatementCoverage;
import com.esep.analytics.service.MonthCoverage;
import com.esep.entity.BankType;
import com.esep.merchantmanagement.interfaces.MerchantReadQuery;
import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantresolver.model.MerchantMatch;
import com.esep.moneyflow.model.MoneyFlowTotal;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.moneyflow.model.MonthlyMoneyFlowTotal;
import com.esep.moneyflow.service.MoneyFlowClassifierRegistry;
import com.esep.moneyflow.service.MoneyFlowReportCalculator;
import com.esep.onetimecheck.model.CategorySpending;
import com.esep.onetimecheck.model.OneTimeCheckReport;
import com.esep.statementimport.interfaces.StatementParserRegistry;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;
import com.esep.statementimport.service.StatementPeriod;
import com.esep.statementimport.service.StatementPeriodResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Разовая проверка выписки без регистрации (ADR-0007): разбор, классификация и отчёт полностью в памяти.
 *
 * <p>Сервис не зависит ни от одного порта записи: выписка, операции и результаты не сохраняются.
 * Назначение переводов неизвестно — у анонимного посетителя нет личных правил, и отчёт показывает долю неизвестного.
 */
@Service
public class OneTimeCheckService {

    private static final String UNKNOWN_MERCHANT = "";
    private static final String UNCATEGORIZED = "Без категории";

    private final StatementParserRegistry statementParserRegistry;
    private final StatementPeriodResolver statementPeriodResolver;
    private final MoneyFlowClassifierRegistry moneyFlowClassifierRegistry;
    private final MerchantRecognitionService merchantRecognitionService;
    private final MerchantReadQuery merchantReadQuery;
    private final MoneyFlowReportCalculator calculator = new MoneyFlowReportCalculator();

    public OneTimeCheckService(
            StatementParserRegistry statementParserRegistry,
            StatementPeriodResolver statementPeriodResolver,
            MoneyFlowClassifierRegistry moneyFlowClassifierRegistry,
            MerchantRecognitionService merchantRecognitionService,
            MerchantReadQuery merchantReadQuery
    ) {
        this.statementParserRegistry = statementParserRegistry;
        this.statementPeriodResolver = statementPeriodResolver;
        this.moneyFlowClassifierRegistry = moneyFlowClassifierRegistry;
        this.merchantRecognitionService = merchantRecognitionService;
        this.merchantReadQuery = merchantReadQuery;
    }

    public OneTimeCheckReport check(InputStream input, BankType bankType) {
        ParsedStatement statement = statementParserRegistry.getParser(bankType).parse(input);
        StatementPeriod period = statementPeriodResolver.resolve(statement);

        Map<TotalKey, Totals> totals = new TreeMap<>();
        int purchasesWithMerchant = 0;
        int purchasesWithoutMerchant = 0;
        Map<String, BigDecimal> spendingByMerchant = new HashMap<>();
        for (ParsedTransaction transaction : statement.transactions()) {
            MoneyFlowType flowType = moneyFlowClassifierRegistry.classify(
                    bankType, transaction.bankOperationType(), transaction.amount(), transaction.description()).flowType();
            totals.computeIfAbsent(new TotalKey(YearMonth.from(transaction.date()), flowType), ignored -> new Totals())
                    .add(transaction.amount());
            if (flowType == MoneyFlowType.PURCHASE || flowType == MoneyFlowType.PURCHASE_REFUND) {
                MerchantMatch match = merchantRecognitionService.recognize(transaction.description()).merchantMatch();
                if (match.matched()) {
                    purchasesWithMerchant++;
                } else {
                    purchasesWithoutMerchant++;
                }
                // Покупка увеличивает траты, возврат уменьшает: amount у покупки отрицательный, у возврата положительный.
                spendingByMerchant.merge(match.matched() ? match.merchantReference().value() : UNKNOWN_MERCHANT,
                        transaction.amount().negate(), BigDecimal::add);
            }
        }

        List<MonthlyMoneyFlowTotal> monthlyTotals = new ArrayList<>();
        totals.forEach((key, value) -> monthlyTotals.add(new MonthlyMoneyFlowTotal(key.month(), value.toTotal(key.flowType()))));
        List<StatementCoverage> coverage = List.of(new StatementCoverage(period.periodFrom(), period.periodTo()));
        List<MonthlyMoneyFlow> monthly = calculator.calculateMonthly(monthlyTotals).stream()
                .map(report -> new MonthlyMoneyFlow(report.month(), report.report(), MonthCoverage.isComplete(report.month(), coverage)))
                .toList();

        return new OneTimeCheckReport(
                period.periodFrom(),
                period.periodTo(),
                statement.transactions().size(),
                calculator.calculate(monthlyTotals.stream().map(MonthlyMoneyFlowTotal::total).toList()),
                monthly,
                purchasesWithMerchant,
                purchasesWithoutMerchant,
                categorySpending(spendingByMerchant)
        );
    }

    private List<CategorySpending> categorySpending(Map<String, BigDecimal> spendingByMerchant) {
        Map<String, String> categoryByMerchant = new HashMap<>();
        if (spendingByMerchant.keySet().stream().anyMatch(key -> !key.equals(UNKNOWN_MERCHANT))) {
            merchantReadQuery.findAll().forEach(merchant ->
                    categoryByMerchant.put(merchant.merchantReference().value(), merchant.categoryName()));
        }
        Map<String, BigDecimal> byCategory = new HashMap<>();
        spendingByMerchant.forEach((merchant, amount) -> byCategory.merge(
                categoryByMerchant.getOrDefault(merchant, UNCATEGORIZED), amount, BigDecimal::add));
        return byCategory.entrySet().stream()
                .filter(entry -> entry.getValue().signum() > 0)
                .map(entry -> new CategorySpending(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategorySpending::amount).reversed().thenComparing(CategorySpending::category))
                .toList();
    }

    private record TotalKey(YearMonth month, MoneyFlowType flowType) implements Comparable<TotalKey> {

        @Override
        public int compareTo(TotalKey other) {
            int byMonth = month.compareTo(other.month);
            return byMonth != 0 ? byMonth : flowType.compareTo(other.flowType);
        }
    }

    private static final class Totals {
        private BigDecimal inflow = BigDecimal.ZERO;
        private BigDecimal outflow = BigDecimal.ZERO;
        private long operationCount;

        void add(BigDecimal amount) {
            if (amount.signum() > 0) {
                inflow = inflow.add(amount);
            } else {
                outflow = outflow.add(amount.negate());
            }
            operationCount++;
        }

        MoneyFlowTotal toTotal(MoneyFlowType flowType) {
            return new MoneyFlowTotal(flowType, MoneyPurpose.UNKNOWN, inflow, outflow, operationCount);
        }
    }
}
