package com.esep.merchantmanagement.integration;

import com.esep.entity.BankType;
import com.esep.entity.Category;
import com.esep.entity.DetectionSource;
import com.esep.entity.Merchant;
import com.esep.entity.MerchantAlias;
import com.esep.entity.MerchantType;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.entity.TransactionType;
import com.esep.merchantmanagement.model.KnowledgeBaseAlias;
import com.esep.merchantmanagement.model.KnowledgeBaseMerchant;
import com.esep.merchantmanagement.service.KnowledgeBaseMerchantService;
import com.esep.normalization.service.DefaultMerchantNormalizer;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({KnowledgeBaseMerchantService.class, MerchantReferenceJpaMapper.class, DefaultMerchantNormalizer.class})
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class KnowledgeBaseMerchantListIntegrationTest {

    @Autowired
    private KnowledgeBaseMerchantService service;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldAggregateOperationsPerMerchantAndSortAliases() {
        Category grocery = Category.builder().code("GROCERY").name("Продукты").active(true).build();
        entityManager.persist(grocery);
        Merchant store = merchant("TEST STORE", grocery);
        Merchant idle = merchant("IDLE SHOP", grocery);
        alias(store, "TEST STORE 2");
        alias(store, "TEST STORE 1");
        Statement statement = Statement.builder().bankName(BankType.KASPI).originalFileName("synthetic.pdf")
                .sourceFileHash("a".repeat(64)).periodFrom(LocalDate.of(2026, 7, 1)).periodTo(LocalDate.of(2026, 8, 31)).build();
        entityManager.persist(statement);
        operation(statement, store, "-1000", LocalDate.of(2026, 7, 3), "1");
        operation(statement, store, "250", LocalDate.of(2026, 8, 9), "2");
        entityManager.flush();
        entityManager.clear();

        List<KnowledgeBaseMerchant> merchants = service.findAll(null, null, null);
        KnowledgeBaseMerchant withOperations = merchants.stream().filter(merchant -> merchant.name().equals("TEST STORE")).findFirst().orElseThrow();
        KnowledgeBaseMerchant withoutOperations = merchants.stream().filter(merchant -> merchant.name().equals("IDLE SHOP")).findFirst().orElseThrow();

        assertThat(withOperations.transactionCount()).isEqualTo(2);
        assertThat(withOperations.totalAmount()).isEqualByComparingTo("1250");
        assertThat(withOperations.firstTransactionDate()).isEqualTo(LocalDate.of(2026, 7, 3));
        assertThat(withOperations.lastTransactionDate()).isEqualTo(LocalDate.of(2026, 8, 9));
        assertThat(withOperations.aliases()).extracting(KnowledgeBaseAlias::aliasName).containsExactly("TEST STORE 1", "TEST STORE 2");
        assertThat(withoutOperations.transactionCount()).isZero();
        assertThat(withoutOperations.totalAmount()).isEqualByComparingTo("0");
        assertThat(withoutOperations.lastTransactionDate()).isNull();
        assertThat(service.findById(withOperations.merchantReference().value())).isEqualTo(withOperations);
    }

    private Merchant merchant(String name, Category category) {
        Merchant merchant = Merchant.builder().originalName(name).normalizedName(name).merchantType(MerchantType.STORE).category(category)
                .confidence(BigDecimal.ONE).detectionSource(DetectionSource.DATABASE).verified(true).build();
        entityManager.persist(merchant);
        return merchant;
    }

    private void alias(Merchant merchant, String name) {
        entityManager.persist(MerchantAlias.builder().aliasName(name).normalizedAlias(name).merchant(merchant).verified(true).build());
    }

    private void operation(Statement statement, Merchant merchant, String amount, LocalDate date, String fingerprint) {
        entityManager.persist(Transaction.builder().statement(statement).merchant(merchant).transactionDate(date)
                .amount(new BigDecimal(amount)).currency("KZT").description("Operation " + fingerprint)
                .transactionType(TransactionType.UNKNOWN).sourceTransactionFingerprint(fingerprint.repeat(64)).build());
    }
}
