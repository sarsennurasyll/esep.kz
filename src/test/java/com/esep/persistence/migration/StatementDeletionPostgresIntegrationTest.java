package com.esep.persistence.migration;

import com.esep.persistence.jpa.adapter.StatementDeletionCatalogJpaAdapter;
import com.esep.statementimport.service.StatementDeletionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверяет удаление выписок на PostgreSQL со схемой и внешними ключами из миграций Flyway. Пропускается без Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({StatementDeletionService.class, StatementDeletionCatalogJpaAdapter.class})
@TestPropertySource(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
@Testcontainers(disabledWithoutDocker = true)
@TestExecutionListeners(listeners = TestOwnerListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class StatementDeletionPostgresIntegrationTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private StatementDeletionService service;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void prepareOwner() {
        TestOwnerListener.prepareOwner(jdbc);
    }

    @Test
    void shouldDeleteStatementGroupWithOperationsOnPostgres() {
        long earlier = statement("a", "2026-04-01", "2026-10-31");
        long later = statement("b", "2026-09-01", "2026-12-31");
        long otherCard = statement("c", "2026-04-01", "2026-12-31");
        jdbc.update("update statements set account_number = '****1111' where id = ?", otherCard);
        operation(earlier, "1");
        operation(later, "2");
        operation(otherCard, "3");

        var plan = service.plan(earlier);
        service.delete(earlier, plan.statementIds());

        assertThat(plan.statementIds()).containsExactly(earlier, later);
        assertThat(jdbc.queryForList("select id from statements", Long.class)).containsExactly(otherCard);
        assertThat(jdbc.queryForObject("select count(*) from transactions", Long.class)).isEqualTo(1);
        assertThat(service.plan(otherCard).statementIds()).isEqualTo(List.of(otherCard));
    }

    private long statement(String hash, String from, String to) {
        jdbc.update("""
                insert into statements (bank_name, original_file_name, source_file_hash, uploaded_at, period_from, period_to, account_number)
                values ('KASPI', 'synthetic.pdf', repeat(?, 64), now(), cast(? as date), cast(? as date), '****0000')""", hash, from, to);
        return jdbc.queryForObject("select id from statements where source_file_hash = repeat(?, 64)", Long.class, hash);
    }

    private void operation(long statementId, String fingerprint) {
        jdbc.update("""
                insert into transactions (amount, currency, transaction_date, source_transaction_fingerprint, description,
                                          transaction_type, bank_operation_type, flow_type, statement_id)
                values (1000, 'KZT', date '2026-10-15', repeat(?, 64), 'Тестов Т.', 'INCOME', 'TOP_UP', 'TRANSFER_FROM_PERSON', ?)""",
                fingerprint, statementId);
    }
}
