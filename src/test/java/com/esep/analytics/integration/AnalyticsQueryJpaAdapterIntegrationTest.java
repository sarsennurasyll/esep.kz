package com.esep.analytics.integration;

import com.esep.analytics.model.AnalyticsSummary;
import com.esep.entity.BankType;
import com.esep.entity.Category;
import com.esep.entity.DetectionSource;
import com.esep.entity.Merchant;
import com.esep.entity.MerchantType;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.entity.TransactionType;
import com.esep.persistence.jpa.read.adapter.AnalyticsQueryJpaAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(AnalyticsQueryJpaAdapter.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AnalyticsQueryJpaAdapterIntegrationTest {

    @Autowired
    private AnalyticsQueryJpaAdapter analyticsQuery;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnZeroAnalyticsForEmptyDatabase() {
        assertThat(analyticsQuery.getSummary()).isEqualTo(new AnalyticsSummary(
                0, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0
        ));
        assertThat(analyticsQuery.getCategoryExpenses()).isEmpty();
        assertThat(analyticsQuery.getTopMerchants()).isEmpty();
        assertThat(analyticsQuery.getMonthlyAnalytics()).isEmpty();
    }

    @Test
    void shouldAggregateSeveralStatementsCategoriesAndUnknownMerchants() {
        Category grocery = category("GROCERY", "Продукты");
        Category pharmacy = category("PHARMACY", "Аптеки");
        Category transfers = category("PERSONAL_TRANSFERS", "Переводы людям");
        Merchant magnum = merchant("MAGNUM", grocery, MerchantType.STORE);
        Merchant europharma = merchant("EUROPHARMA", pharmacy, MerchantType.PHARMACY);
        Merchant erasyl = merchant("ЕРАСЫЛ Е", transfers, MerchantType.PERSON);
        Statement july = statement("a");
        Statement august = statement("b");

        transaction(july, magnum, LocalDate.of(2026, 7, 2), new BigDecimal("-100.00"), "1");
        transaction(july, europharma, LocalDate.of(2026, 7, 3), new BigDecimal("-200.00"), "2");
        transaction(july, null, LocalDate.of(2026, 7, 4), new BigDecimal("-50.00"), "3");
        transaction(july, magnum, LocalDate.of(2026, 7, 5), new BigDecimal("1000.00"), "4");
        transaction(august, null, LocalDate.of(2026, 8, 1), new BigDecimal("-300.00"), "5");
        transaction(august, erasyl, LocalDate.of(2026, 8, 2), new BigDecimal("-500.00"), "6");
        entityManager.flush();
        entityManager.clear();

        assertThat(analyticsQuery.getSummary()).isEqualTo(new AnalyticsSummary(
                2, 6, new BigDecimal("1000.00"), new BigDecimal("1150.00"), 4, 2
        ));
        assertThat(analyticsQuery.getCategoryExpenses())
                .extracting(expense -> expense.category() + ":" + expense.amount())
                .containsExactly(
                        "PERSONAL_TRANSFERS:500.00",
                        "UNCATEGORIZED:350.00",
                        "PHARMACY:200.00",
                        "GROCERY:100.00"
                );
        assertThat(analyticsQuery.getCategoryOperationCounts())
                .extracting(count -> count.category() + ":" + count.transactionCount())
                .containsExactlyInAnyOrder(
                        "UNCATEGORIZED:2",
                        "GROCERY:1",
                        "PERSONAL_TRANSFERS:1",
                        "PHARMACY:1"
                );
        assertThat(analyticsQuery.getTopMerchants())
                .extracting(expense -> expense.merchant() + ":" + expense.amount() + ":" + expense.transactionCount())
                .containsExactly("ЕРАСЫЛ Е:500.00:1", "EUROPHARMA:200.00:1", "MAGNUM:100.00:1");
        assertThat(analyticsQuery.getMerchantTypeExpenses())
                .extracting(expense -> expense.merchantType() + ":" + expense.amount() + ":" + expense.transactionCount())
                .containsExactly("PERSON:500.00:1", "PHARMACY:200.00:1", "STORE:100.00:1");
        assertThat(analyticsQuery.getTopPersonTransfers())
                .containsExactly(new com.esep.analytics.model.PersonTransfer("ЕРАСЫЛ Е", new BigDecimal("500.00"), 1));
        assertThat(analyticsQuery.getMonthlyAnalytics())
                .extracting(month -> month.month() + ":" + month.income() + ":" + month.expense())
                .containsExactly("2026-07:1000.00:350.00", "2026-08:0:800.00");
    }

    private Category category(String code, String name) {
        Category category = Category.builder().code(code).name(name).active(true).build();
        entityManager.persist(category);
        return category;
    }

    private Merchant merchant(String name, Category category, MerchantType merchantType) {
        Merchant merchant = Merchant.builder()
                .originalName(name)
                .normalizedName(name)
                .category(category)
                .merchantType(merchantType)
                .confidence(BigDecimal.ONE)
                .detectionSource(DetectionSource.DATABASE)
                .verified(true)
                .build();
        entityManager.persist(merchant);
        return merchant;
    }

    private Statement statement(String hashCharacter) {
        Statement statement = Statement.builder()
                .bankName(BankType.KASPI)
                .originalFileName(hashCharacter + ".pdf")
                .sourceFileHash(hashCharacter.repeat(64))
                .periodFrom(LocalDate.of(2026, 7, 1))
                .periodTo(LocalDate.of(2026, 8, 31))
                .build();
        entityManager.persist(statement);
        return statement;
    }

    private void transaction(
            Statement statement,
            Merchant merchant,
            LocalDate date,
            BigDecimal amount,
            String fingerprintCharacter
    ) {
        entityManager.persist(Transaction.builder()
                .statement(statement)
                .merchant(merchant)
                .transactionDate(date)
                .amount(amount)
                .currency("KZT")
                .description("Operation " + fingerprintCharacter)
                .sourceTransactionFingerprint(fingerprintCharacter.repeat(64))
                .transactionType(TransactionType.UNKNOWN)
                .build());
    }
}
