package com.esep.moneyflow.integration;

import com.esep.entity.BankType;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.entity.TransactionType;
import com.esep.moneyflow.exception.OperationNotFoundException;
import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.CounterpartyLabelingItem;
import com.esep.moneyflow.model.CounterpartyOperation;
import com.esep.moneyflow.model.CounterpartyPurposeRule;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.moneyflow.model.PurposeDecision;
import com.esep.moneyflow.service.PurposeLabelingService;
import com.esep.persistence.jpa.adapter.CounterpartyPurposeRuleCatalogJpaAdapter;
import com.esep.persistence.jpa.read.adapter.PurposeLabelingTransactionsJpaAdapter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({PurposeLabelingService.class, CounterpartyPurposeRuleCatalogJpaAdapter.class, PurposeLabelingTransactionsJpaAdapter.class})
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class PurposeLabelingServiceIntegrationTest {

    @Autowired
    private PurposeLabelingService service;

    @Autowired
    private EntityManager entityManager;

    private Statement statement;

    @BeforeEach
    void setUp() {
        statement = Statement.builder().bankName(BankType.KASPI).originalFileName("synthetic.pdf")
                .sourceFileHash("a".repeat(64)).periodFrom(LocalDate.of(2026, 8, 1)).periodTo(LocalDate.of(2026, 8, 31)).build();
        entityManager.persist(statement);
    }

    @Test
    void shouldApplyRuleToCounterpartyInOneDirectionOnly() {
        operation("Тестов Т.", "50000", MoneyFlowType.TRANSFER_FROM_PERSON, "1");
        operation("ТЕСТОВ  Т.", "20000", MoneyFlowType.TRANSFER_FROM_PERSON, "2");
        operation("Тестов Т.", "-5000", MoneyFlowType.TRANSFER_TO_PERSON, "3");
        operation("Тестова А.", "7000", MoneyFlowType.TRANSFER_FROM_PERSON, "4");
        entityManager.flush();

        service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);

        assertThat(purposeOf("1")).isEqualTo(MoneyPurpose.INCOME_WORK);
        assertThat(purposeOf("2")).isEqualTo(MoneyPurpose.INCOME_WORK);
        assertThat(purposeOf("3")).isEqualTo(MoneyPurpose.UNKNOWN);
        assertThat(purposeOf("4")).isEqualTo(MoneyPurpose.UNKNOWN);
        assertThat(sourceOf("1")).isEqualTo(ClassificationSource.PERSONAL_RULE);
    }

    @Test
    void shouldNotOverrideUserDecisionOrTouchPurchases() {
        operation("Тестов Т.", "50000", MoneyFlowType.TRANSFER_FROM_PERSON, "1");
        Transaction manual = operation("Тестов Т.", "30000", MoneyFlowType.TRANSFER_FROM_PERSON, "2");
        manual.setPurpose(MoneyPurpose.DEBT);
        manual.setPurposeSource(ClassificationSource.USER);
        operation("Тестов Т.", "1500", MoneyFlowType.PURCHASE_REFUND, "3");
        entityManager.flush();

        service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.FAMILY);

        assertThat(purposeOf("1")).isEqualTo(MoneyPurpose.FAMILY);
        assertThat(purposeOf("2")).isEqualTo(MoneyPurpose.DEBT);
        assertThat(purposeOf("3")).isEqualTo(MoneyPurpose.UNKNOWN);
    }

    @Test
    void shouldReturnRuleLabelsToUnknownWhenRuleIsRemoved() {
        operation("Тестов Т.", "50000", MoneyFlowType.TRANSFER_FROM_PERSON, "1");
        Transaction manual = operation("Тестов Т.", "30000", MoneyFlowType.TRANSFER_FROM_PERSON, "2");
        manual.setPurpose(MoneyPurpose.DEBT);
        manual.setPurposeSource(ClassificationSource.USER);
        entityManager.flush();
        CounterpartyPurposeRule rule = service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);

        service.removeRule(rule.id());

        assertThat(purposeOf("1")).isEqualTo(MoneyPurpose.UNKNOWN);
        assertThat(sourceOf("1")).isEqualTo(ClassificationSource.NONE);
        assertThat(purposeOf("2")).isEqualTo(MoneyPurpose.DEBT);
        assertThat(service.decide("Тестов Т.", new BigDecimal("100"), MoneyFlowType.TRANSFER_FROM_PERSON))
                .isEqualTo(PurposeDecision.unknown());
    }

    @Test
    void shouldListCounterpartiesByAmountAndReportProgress() {
        operation("Тестов Т.", "50000", MoneyFlowType.TRANSFER_FROM_PERSON, "1");
        operation("Тестова А.", "-30000", MoneyFlowType.TRANSFER_TO_PERSON, "2");
        operation("С карты другого банка", "20000", MoneyFlowType.UNKNOWN, "3");
        operation("TEST STORE", "-9000", MoneyFlowType.PURCHASE, "4");
        entityManager.flush();
        service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);

        assertThat(service.findCounterparties())
                .extracting(CounterpartyLabelingItem::exampleDescription)
                .containsExactly("Тестов Т.", "Тестова А.", "С карты другого банка");
        var progress = service.progress();
        assertThat(progress.totalAmount()).isEqualByComparingTo("100000");
        assertThat(progress.labeledAmount()).isEqualByComparingTo("50000");
        assertThat(progress.labeledShare()).isEqualByComparingTo("0.5");
        assertThat(progress.rules()).isEqualTo(1);
    }

    @Test
    void shouldUseRuleForNewOperationsDuringImport() {
        service.setRule("Тестов Т.", MoneyDirection.OUTFLOW, MoneyPurpose.FAMILY);

        assertThat(service.decide("ТЕСТОВ Т.", new BigDecimal("-100"), MoneyFlowType.TRANSFER_TO_PERSON))
                .isEqualTo(new PurposeDecision(MoneyPurpose.FAMILY, ClassificationSource.PERSONAL_RULE));
        assertThat(service.decide("Тестов Т.", new BigDecimal("100"), MoneyFlowType.TRANSFER_FROM_PERSON))
                .isEqualTo(PurposeDecision.unknown());
        assertThat(service.decide("Тестов Т.", new BigDecimal("-100"), MoneyFlowType.PURCHASE))
                .isEqualTo(PurposeDecision.unknown());
    }

    @Test
    void shouldRejectPurposeThatContradictsDirection() {
        assertThatThrownBy(() -> service.setRule("Тестов Т.", MoneyDirection.OUTFLOW, MoneyPurpose.INCOME_WORK))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.UNKNOWN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldKeepOperationPurposeAboveCounterpartyRule() {
        Transaction loanReturn = operation("Тестов Т.", "30000", MoneyFlowType.TRANSFER_FROM_PERSON, "1");
        operation("Тестов Т.", "50000", MoneyFlowType.TRANSFER_FROM_PERSON, "2");
        entityManager.flush();

        service.setOperationPurpose(loanReturn.getId(), MoneyPurpose.DEBT);
        CounterpartyPurposeRule rule = service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);

        assertThat(purposeOf("1")).isEqualTo(MoneyPurpose.DEBT);
        assertThat(sourceOf("1")).isEqualTo(ClassificationSource.USER);
        assertThat(purposeOf("2")).isEqualTo(MoneyPurpose.INCOME_WORK);

        service.removeRule(rule.id());

        assertThat(purposeOf("1")).isEqualTo(MoneyPurpose.DEBT);
        assertThat(purposeOf("2")).isEqualTo(MoneyPurpose.UNKNOWN);
    }

    @Test
    void shouldCountManualExceptionsPerCounterparty() {
        Transaction manual = operation("Тестов Т.", "30000", MoneyFlowType.TRANSFER_FROM_PERSON, "1");
        operation("Тестов Т.", "50000", MoneyFlowType.TRANSFER_FROM_PERSON, "2");
        entityManager.flush();
        service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);
        service.setOperationPurpose(manual.getId(), MoneyPurpose.DEBT);

        CounterpartyLabelingItem item = service.findCounterparties().getFirst();

        assertThat(item.operationCount()).isEqualTo(2);
        assertThat(item.manualOperationCount()).isEqualTo(1);
        assertThat(item.labeledAmount()).isEqualByComparingTo("80000");
    }

    @Test
    void shouldListCounterpartyOperationsInOneDirectionWithTheirSources() {
        operation("Тестов Т.", "50000", MoneyFlowType.TRANSFER_FROM_PERSON, "1");
        Transaction manual = operation("ТЕСТОВ  Т.", "30000", MoneyFlowType.TRANSFER_FROM_PERSON, "2");
        operation("Тестов Т.", "-7000", MoneyFlowType.TRANSFER_TO_PERSON, "3");
        operation("Тестов Т.", "1500", MoneyFlowType.PURCHASE_REFUND, "4");
        operation("Вымыслов В.", "9000", MoneyFlowType.TRANSFER_FROM_PERSON, "5");
        entityManager.flush();
        service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);
        service.setOperationPurpose(manual.getId(), MoneyPurpose.DEBT);

        var operations = service.findCounterpartyOperations("тестов т.", MoneyDirection.INFLOW);

        assertThat(operations).extracting(CounterpartyOperation::id).containsExactly(manual.getId(), transaction("1").getId());
        assertThat(operations).extracting(CounterpartyOperation::purposeSource)
                .containsExactly(ClassificationSource.USER, ClassificationSource.PERSONAL_RULE);
        assertThat(operations.getFirst().statementId()).isEqualTo(statement.getId());
        assertThat(service.findCounterpartyOperations(" ", MoneyDirection.INFLOW)).isEmpty();
    }

    @Test
    void shouldReturnToCounterpartyRuleWhenOperationPurposeIsCleared() {
        Transaction withRule = operation("Тестов Т.", "30000", MoneyFlowType.TRANSFER_FROM_PERSON, "1");
        Transaction withoutRule = operation("Тестова А.", "-7000", MoneyFlowType.TRANSFER_TO_PERSON, "2");
        entityManager.flush();
        service.setRule("Тестов Т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);
        service.setOperationPurpose(withRule.getId(), MoneyPurpose.GIFT);
        service.setOperationPurpose(withoutRule.getId(), MoneyPurpose.FAMILY);

        service.clearOperationPurpose(withRule.getId());
        service.clearOperationPurpose(withoutRule.getId());

        assertThat(purposeOf("1")).isEqualTo(MoneyPurpose.INCOME_WORK);
        assertThat(sourceOf("1")).isEqualTo(ClassificationSource.PERSONAL_RULE);
        assertThat(purposeOf("2")).isEqualTo(MoneyPurpose.UNKNOWN);
        assertThat(sourceOf("2")).isEqualTo(ClassificationSource.NONE);
    }

    @Test
    void shouldRejectInvalidOperationPurpose() {
        Transaction purchase = operation("TEST STORE", "-1000", MoneyFlowType.PURCHASE, "1");
        Transaction outgoing = operation("Тестов Т.", "-1000", MoneyFlowType.TRANSFER_TO_PERSON, "2");
        entityManager.flush();

        assertThatThrownBy(() -> service.setOperationPurpose(purchase.getId(), MoneyPurpose.EXPENSE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.setOperationPurpose(outgoing.getId(), MoneyPurpose.INCOME_WORK))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.setOperationPurpose(outgoing.getId(), MoneyPurpose.UNKNOWN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.setOperationPurpose(999_999L, MoneyPurpose.FAMILY))
                .isInstanceOf(OperationNotFoundException.class);
        assertThat(purposeOf("2")).isEqualTo(MoneyPurpose.UNKNOWN);
    }

    private Transaction operation(String description, String amount, MoneyFlowType flowType, String fingerprint) {
        Transaction transaction = Transaction.builder().statement(statement).transactionDate(LocalDate.of(2026, 8, 2))
                .amount(new BigDecimal(amount)).currency("KZT").description(description)
                .transactionType(TransactionType.UNKNOWN).flowType(flowType)
                .sourceTransactionFingerprint(fingerprint.repeat(64)).build();
        entityManager.persist(transaction);
        return transaction;
    }

    private MoneyPurpose purposeOf(String fingerprint) {
        return transaction(fingerprint).getPurpose();
    }

    private ClassificationSource sourceOf(String fingerprint) {
        return transaction(fingerprint).getPurposeSource();
    }

    private Transaction transaction(String fingerprint) {
        entityManager.clear();
        return entityManager.createQuery(
                "select t from Transaction t where t.sourceTransactionFingerprint = :fingerprint", Transaction.class)
                .setParameter("fingerprint", fingerprint.repeat(64))
                .getSingleResult();
    }
}
