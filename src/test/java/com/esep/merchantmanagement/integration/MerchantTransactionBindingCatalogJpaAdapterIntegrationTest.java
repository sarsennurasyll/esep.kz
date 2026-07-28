package com.esep.merchantmanagement.integration;

import com.esep.entity.BankType;
import com.esep.entity.Category;
import com.esep.entity.DetectionSource;
import com.esep.entity.Merchant;
import com.esep.entity.MerchantType;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.entity.TransactionType;
import com.esep.merchantmanagement.interfaces.MerchantTransactionBindingCatalog;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.persistence.jpa.adapter.MerchantTransactionBindingCatalogJpaAdapter;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({MerchantTransactionBindingCatalogJpaAdapter.class, MerchantReferenceJpaMapper.class})
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class MerchantTransactionBindingCatalogJpaAdapterIntegrationTest {

    @Autowired
    private MerchantTransactionBindingCatalog bindingCatalog;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldBindAllUnknownTransactionsWithMatchingDescriptionsOnly() {
        Category category = Category.builder().code("GROCERY").name("Products").active(true).build();
        entityManager.persist(category);
        Merchant merchant = Merchant.builder().originalName("MAGNUM").normalizedName("MAGNUM")
                .category(category).merchantType(MerchantType.STORE).confidence(BigDecimal.ONE)
                .detectionSource(DetectionSource.DATABASE).verified(true).build();
        entityManager.persist(merchant);
        Statement statement = Statement.builder().bankName(BankType.KASPI).originalFileName("test.pdf")
                .sourceFileHash("a".repeat(64)).periodFrom(LocalDate.of(2026, 7, 1)).periodTo(LocalDate.of(2026, 7, 1)).build();
        entityManager.persist(statement);
        entityManager.persist(transaction(statement, "Magnum Store", "b"));
        entityManager.persist(transaction(statement, "Magnum Store", "c"));
        entityManager.persist(transaction(statement, "Other Shop", "d"));
        entityManager.flush();

        long bound = bindingCatalog.bindUnknownTransactions(List.of("Magnum Store"), new MerchantReference(merchant.getId().toString()));
        entityManager.flush();
        entityManager.clear();

        assertThat(bound).isEqualTo(2);
        List<Transaction> transactions = entityManager.createQuery("select transaction from Transaction transaction order by transaction.description", Transaction.class).getResultList();
        assertThat(transactions).extracting(transaction -> transaction.getMerchant() == null ? null : transaction.getMerchant().getNormalizedName())
                .containsExactly("MAGNUM", "MAGNUM", null);
    }

    private Transaction transaction(Statement statement, String description, String fingerprintCharacter) {
        return Transaction.builder().statement(statement).transactionDate(LocalDate.of(2026, 7, 1))
                .amount(new BigDecimal("-100.00")).currency("KZT").description(description)
                .sourceTransactionFingerprint(fingerprintCharacter.repeat(64)).transactionType(TransactionType.EXPENSE).build();
    }
}
