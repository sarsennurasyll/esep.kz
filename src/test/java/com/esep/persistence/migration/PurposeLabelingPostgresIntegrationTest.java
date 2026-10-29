package com.esep.persistence.migration;

import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.moneyflow.service.PurposeLabelingService;
import com.esep.persistence.jpa.adapter.CounterpartyPurposeRuleCatalogJpaAdapter;
import com.esep.persistence.jpa.read.adapter.PurposeLabelingTransactionsJpaAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверяет запросы разметки назначения на PostgreSQL со схемой из миграций Flyway. Пропускается без Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PurposeLabelingService.class, CounterpartyPurposeRuleCatalogJpaAdapter.class, PurposeLabelingTransactionsJpaAdapter.class})
@TestPropertySource(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
@Testcontainers(disabledWithoutDocker = true)
class PurposeLabelingPostgresIntegrationTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PurposeLabelingService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldApplyAndRemoveRuleOnPostgres() {
        jdbc.update("""
                insert into statements (bank_name, original_file_name, source_file_hash, uploaded_at, period_from, period_to)
                values ('KASPI', 'synthetic.pdf', repeat('a', 64), now(), date '2026-08-01', date '2026-08-31')""");
        jdbc.update("""
                insert into transactions (amount, currency, transaction_date, source_transaction_fingerprint, description,
                                          transaction_type, bank_operation_type, flow_type, statement_id)
                values (50000, 'KZT', date '2026-08-02', repeat('b', 64), 'Тестов Т.', 'INCOME', 'TOP_UP', 'TRANSFER_FROM_PERSON',
                        (select id from statements)),
                       (-5000, 'KZT', date '2026-08-03', repeat('c', 64), 'Тестов Т.', 'TRANSFER', 'TRANSFER', 'TRANSFER_TO_PERSON',
                        (select id from statements))""");

        var rule = service.setRule("тестов т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);

        assertThat(jdbc.queryForList("select purpose || ':' || purpose_source from transactions order by amount", String.class))
                .containsExactly("UNKNOWN:NONE", "INCOME_WORK:PERSONAL_RULE");
        assertThat(service.progress().labeledShare()).isEqualByComparingTo("0.9091");

        service.removeRule(rule.id());

        assertThat(jdbc.queryForList("select purpose || ':' || purpose_source from transactions", String.class))
                .containsOnly("UNKNOWN:NONE");
        assertThat(jdbc.queryForObject("select count(*) from counterparty_purpose_rules", Long.class)).isZero();
    }

    @Test
    void shouldKeepOperationPurposeAboveRuleOnPostgres() {
        jdbc.update("""
                insert into statements (bank_name, original_file_name, source_file_hash, uploaded_at, period_from, period_to)
                values ('KASPI', 'synthetic.pdf', repeat('d', 64), now(), date '2026-08-01', date '2026-08-31')""");
        jdbc.update("""
                insert into transactions (amount, currency, transaction_date, source_transaction_fingerprint, description,
                                          transaction_type, bank_operation_type, flow_type, statement_id)
                values (30000, 'KZT', date '2026-08-02', repeat('e', 64), 'Тестова А.', 'INCOME', 'TOP_UP', 'TRANSFER_FROM_PERSON',
                        (select id from statements where source_file_hash = repeat('d', 64)))""");
        long operationId = jdbc.queryForObject("select id from transactions where source_transaction_fingerprint = repeat('e', 64)", Long.class);

        service.setOperationPurpose(operationId, MoneyPurpose.DEBT);
        service.setRule("Тестова А.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);
        String afterRule = jdbc.queryForObject("select purpose || ':' || purpose_source from transactions where id = ?", String.class, operationId);
        var counterpartyOperations = service.findCounterpartyOperations("тестова а.", MoneyDirection.INFLOW);
        service.clearOperationPurpose(operationId);
        String afterClear = jdbc.queryForObject("select purpose || ':' || purpose_source from transactions where id = ?", String.class, operationId);

        assertThat(afterRule).isEqualTo("DEBT:USER");
        assertThat(afterClear).isEqualTo("INCOME_WORK:PERSONAL_RULE");
        assertThat(counterpartyOperations).singleElement().satisfies(operation -> {
            assertThat(operation.id()).isEqualTo(operationId);
            assertThat(operation.purpose()).isEqualTo(MoneyPurpose.DEBT);
        });
    }
}
