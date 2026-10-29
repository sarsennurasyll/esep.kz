package com.esep.statementimport.integration;

import com.esep.entity.BankType;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.entity.TransactionType;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.persistence.jpa.adapter.CounterpartyPurposeRuleCatalogJpaAdapter;
import com.esep.persistence.jpa.adapter.StatementDeletionCatalogJpaAdapter;
import com.esep.statementimport.exception.StatementDeletionMismatchException;
import com.esep.statementimport.exception.StatementNotFoundException;
import com.esep.statementimport.model.StatementDeletionPlan;
import com.esep.statementimport.service.StatementDeletionService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({StatementDeletionService.class, StatementDeletionCatalogJpaAdapter.class, CounterpartyPurposeRuleCatalogJpaAdapter.class})
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class StatementDeletionServiceIntegrationTest {

    @Autowired
    private StatementDeletionService service;

    @Autowired
    private CounterpartyPurposeRuleCatalogJpaAdapter rules;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldDeleteLaterStatementWithItsOperationsOnly() {
        Statement earlier = statement("a", 4, 10, 2);
        Statement later = statement("b", 9, 12, 3);

        StatementDeletionPlan deleted = service.delete(later.getId(), List.of(later.getId()));

        assertThat(deleted.statementIds()).containsExactly(later.getId());
        assertThat(deleted.operationCount()).isEqualTo(3);
        assertThat(count("Statement")).isEqualTo(1);
        assertThat(count("Transaction")).isEqualTo(2);
        assertThat(entityManager.find(Statement.class, earlier.getId())).isNotNull();
    }

    @Test
    void shouldRequireConfirmingLaterOverlappingStatements() {
        Statement earlier = statement("a", 4, 10, 2);
        Statement later = statement("b", 9, 12, 3);

        StatementDeletionPlan plan = service.plan(earlier.getId());

        assertThat(plan.statementIds()).containsExactly(earlier.getId(), later.getId());
        assertThat(plan.operationCount()).isEqualTo(5);
        assertThatThrownBy(() -> service.delete(earlier.getId(), List.of(earlier.getId())))
                .isInstanceOf(StatementDeletionMismatchException.class);
        assertThat(count("Transaction")).isEqualTo(5);

        service.delete(earlier.getId(), List.of(later.getId(), earlier.getId()));

        assertThat(count("Statement")).isZero();
        assertThat(count("Transaction")).isZero();
    }

    @Test
    void shouldKeepCounterpartyRulesAndAllowReimportOfSameFile() {
        Statement statement = statement("a", 8, 8, 1);
        rules.save("ТЕСТОВ Т.", MoneyDirection.INFLOW, MoneyPurpose.FAMILY);

        service.delete(statement.getId(), List.of(statement.getId()));
        statement("a", 8, 8, 1);

        assertThat(rules.findAll()).hasSize(1);
        assertThat(count("Statement")).isEqualTo(1);
    }

    @Test
    void shouldReportMissingStatement() {
        assertThatThrownBy(() -> service.plan(999_999L)).isInstanceOf(StatementNotFoundException.class);
    }

    private Statement statement(String hash, int fromMonth, int toMonth, int operations) {
        LocalDate to = LocalDate.of(2026, toMonth, 1);
        Statement statement = Statement.builder().bankName(BankType.KASPI).originalFileName("synthetic.pdf").accountNumber("****0000")
                .sourceFileHash(hash.repeat(64)).periodFrom(LocalDate.of(2026, fromMonth, 1)).periodTo(to.withDayOfMonth(to.lengthOfMonth())).build();
        entityManager.persist(statement);
        for (int index = 0; index < operations; index++) {
            entityManager.persist(Transaction.builder().statement(statement).transactionDate(to)
                    .amount(new BigDecimal("1000")).currency("KZT").description("Тестов Т.")
                    .transactionType(TransactionType.UNKNOWN).sourceTransactionFingerprint((hash + index).repeat(32)).build());
        }
        entityManager.flush();
        return statement;
    }

    private long count(String entity) {
        return entityManager.createQuery("select count(e) from " + entity + " e", Long.class).getSingleResult();
    }
}
