package com.esep.merchantmanagement.integration;

import com.esep.entity.BankType;
import com.esep.entity.DetectionSource;
import com.esep.entity.Merchant;
import com.esep.entity.MerchantType;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.entity.TransactionType;
import com.esep.merchantmanagement.model.MerchantLearningStatistics;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.persistence.jpa.read.adapter.MerchantLearningStatisticsJpaAdapter;
import com.esep.persistence.jpa.read.adapter.UnknownMerchantDescriptionJpaAdapter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({MerchantLearningStatisticsJpaAdapter.class, UnknownMerchantDescriptionJpaAdapter.class})
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class MerchantLearningStatisticsJpaAdapterIntegrationTest {

    @Autowired
    private MerchantLearningStatisticsJpaAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldMeasureMerchantCoverageOnPurchasesOnly() {
        Statement statement = Statement.builder().bankName(BankType.KASPI).originalFileName("synthetic.pdf")
                .sourceFileHash("a".repeat(64)).periodFrom(LocalDate.of(2026, 8, 1)).periodTo(LocalDate.of(2026, 8, 31)).build();
        entityManager.persist(statement);
        Merchant store = merchant("TEST STORE", MerchantType.STORE);
        Merchant transfers = merchant("ПЕРЕВОДЫ ЛЮДЯМ", MerchantType.PERSON);
        operation(statement, store, MoneyFlowType.PURCHASE, "-1000", "1");
        operation(statement, store, MoneyFlowType.PURCHASE_REFUND, "500", "2");
        operation(statement, null, MoneyFlowType.PURCHASE, "-700", "3");
        operation(statement, transfers, MoneyFlowType.TRANSFER_TO_PERSON, "-5000", "4");
        operation(statement, transfers, MoneyFlowType.TRANSFER_FROM_PERSON, "9000", "5");
        operation(statement, null, MoneyFlowType.CASH_WITHDRAWAL, "-2000", "6");
        entityManager.flush();

        MerchantLearningStatistics statistics = adapter.getStatistics();

        assertThat(statistics.purchasesWithMerchantCount()).isEqualTo(2);
        assertThat(statistics.purchasesWithoutMerchantCount()).isEqualTo(1);
        assertThat(statistics.merchantCoveragePercent()).isCloseTo(66.67, offset(0.01));
    }

    private Merchant merchant(String name, MerchantType type) {
        Merchant merchant = Merchant.builder().originalName(name).normalizedName(name).merchantType(type)
                .confidence(BigDecimal.ONE).detectionSource(DetectionSource.DATABASE).verified(true).build();
        entityManager.persist(merchant);
        return merchant;
    }

    private void operation(Statement statement, Merchant merchant, MoneyFlowType flowType, String amount, String fingerprint) {
        entityManager.persist(Transaction.builder().statement(statement).merchant(merchant).flowType(flowType)
                .transactionDate(LocalDate.of(2026, 8, 2)).amount(new BigDecimal(amount)).currency("KZT")
                .description("Operation " + fingerprint).transactionType(TransactionType.UNKNOWN)
                .sourceTransactionFingerprint(fingerprint.repeat(64)).build());
    }
}
