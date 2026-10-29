package com.esep.persistence.migration;

import com.esep.analytics.interfaces.AnalyticsQuery;
import com.esep.persistence.jpa.read.adapter.AnalyticsQueryJpaAdapter;
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
 * Проверяет группировку движения денег по месяцам на PostgreSQL. Пропускается без Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AnalyticsQueryJpaAdapter.class)
@TestPropertySource(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
@Testcontainers(disabledWithoutDocker = true)
class MonthlyMoneyFlowPostgresIntegrationTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AnalyticsQuery analyticsQuery;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldGroupMoneyFlowByCalendarMonthOnPostgres() {
        jdbc.update("""
                insert into statements (bank_name, original_file_name, source_file_hash, uploaded_at, period_from, period_to)
                values ('KASPI', 'synthetic.pdf', repeat('a', 64), now(), date '2026-07-01', date '2026-08-31')""");
        jdbc.update("""
                insert into transactions (amount, currency, transaction_date, source_transaction_fingerprint, description,
                                          transaction_type, bank_operation_type, flow_type, statement_id)
                values (-100, 'KZT', date '2026-07-31', repeat('b', 64), 'TEST STORE', 'EXPENSE', 'PURCHASE', 'PURCHASE', (select id from statements)),
                       (-50, 'KZT', date '2026-08-01', repeat('c', 64), 'TEST STORE', 'EXPENSE', 'PURCHASE', 'PURCHASE', (select id from statements)),
                       (-25, 'KZT', date '2026-08-20', repeat('d', 64), 'TEST STORE', 'EXPENSE', 'PURCHASE', 'PURCHASE', (select id from statements))""");

        assertThat(analyticsQuery.getMonthlyMoneyFlowTotals())
                .extracting(total -> total.month() + ":" + total.total().outflow().stripTrailingZeros().toPlainString())
                .containsExactly("2026-07:100", "2026-08:75");
    }
}
