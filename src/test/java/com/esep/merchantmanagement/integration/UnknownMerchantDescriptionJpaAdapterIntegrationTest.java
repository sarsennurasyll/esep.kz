package com.esep.merchantmanagement.integration;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.entity.TransactionType;
import com.esep.merchantmanagement.interfaces.UnknownMerchantDescriptionQuery;
import com.esep.merchantmanagement.model.UnknownMerchantCandidate;
import com.esep.persistence.jpa.read.adapter.UnknownMerchantDescriptionJpaAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(UnknownMerchantDescriptionJpaAdapter.class)
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class UnknownMerchantDescriptionJpaAdapterIntegrationTest {

    @Autowired
    private UnknownMerchantDescriptionQuery unknownMerchantDescriptionQuery;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldOfferOnlyOperationsWhoseCounterpartyCanBeMerchant() {
        Statement statement = Statement.builder().bankName(BankType.KASPI).originalFileName("test.pdf")
                .sourceFileHash("a".repeat(64)).uploadedAt(Instant.parse("2026-08-01T00:00:00Z"))
                .periodFrom(LocalDate.of(2026, 7, 1)).periodTo(LocalDate.of(2026, 7, 31)).build();
        entityManager.persist(statement);
        entityManager.persist(transaction(statement, "TEST STORE", "b", BankOperationType.PURCHASE));
        entityManager.persist(transaction(statement, "LEGACY STORE", "c", BankOperationType.UNKNOWN));
        entityManager.persist(transaction(statement, "Тестов Т.", "d", BankOperationType.TOP_UP));
        entityManager.persist(transaction(statement, "Тестова А.", "e", BankOperationType.TRANSFER));
        entityManager.persist(transaction(statement, "Банкомат TEST STORE", "f", BankOperationType.CASH_WITHDRAWAL));
        entityManager.persist(transaction(statement, "Плата за обслуживание", "g", BankOperationType.OTHER));
        entityManager.flush();

        assertThat(unknownMerchantDescriptionQuery.findAll())
                .extracting(UnknownMerchantCandidate::description)
                .containsExactlyInAnyOrder("TEST STORE", "LEGACY STORE");
    }

    private Transaction transaction(Statement statement, String description, String fingerprintCharacter, BankOperationType operationType) {
        return Transaction.builder().statement(statement).transactionDate(LocalDate.of(2026, 7, 1))
                .amount(new BigDecimal("-100.00")).currency("KZT").description(description).bankOperationType(operationType)
                .sourceTransactionFingerprint(fingerprintCharacter.repeat(64)).transactionType(TransactionType.EXPENSE).build();
    }
}
