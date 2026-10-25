package com.esep.statementimport.integration;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.entity.TransactionType;
import com.esep.persistence.interfaces.TransactionCatalog;
import com.esep.persistence.jpa.adapter.TransactionCatalogJpaAdapter;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import com.esep.persistence.jpa.mapper.TransactionJpaMapper;
import com.esep.persistence.model.ExistingOperationKey;
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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({TransactionCatalogJpaAdapter.class, TransactionJpaMapper.class, MerchantReferenceJpaMapper.class})
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class TransactionOperationKeysJpaIntegrationTest {

    @Autowired
    private TransactionCatalog transactionCatalog;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnOnlyOperationsOfSameBankAccountWithinPeriod() {
        Statement sameAccount = statement("a", "****1234");
        Statement otherAccount = statement("b", "****9999");
        operation(sameAccount, LocalDate.of(2026, 8, 10), "-100.00", "TEST STORE", "c");
        operation(sameAccount, LocalDate.of(2026, 7, 31), "-200.00", "OLD STORE", "d");
        operation(otherAccount, LocalDate.of(2026, 8, 10), "-100.00", "TEST STORE", "e");
        entityManager.flush();

        assertThat(transactionCatalog.findOperationKeys(BankType.KASPI, "****1234", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .extracting(ExistingOperationKey::description)
                .containsExactly("TEST STORE");
    }

    private Statement statement(String hash, String maskedAccount) {
        Statement statement = Statement.builder().bankName(BankType.KASPI).originalFileName(hash + ".pdf")
                .accountNumber(maskedAccount).sourceFileHash(hash.repeat(64))
                .periodFrom(LocalDate.of(2026, 7, 1)).periodTo(LocalDate.of(2026, 8, 31)).build();
        entityManager.persist(statement);
        return statement;
    }

    private void operation(Statement statement, LocalDate date, String amount, String description, String fingerprint) {
        entityManager.persist(Transaction.builder().statement(statement).transactionDate(date)
                .amount(new BigDecimal(amount)).currency("KZT").description(description)
                .bankOperationType(BankOperationType.PURCHASE).transactionType(TransactionType.EXPENSE)
                .sourceTransactionFingerprint(fingerprint.repeat(64)).build());
    }
}
