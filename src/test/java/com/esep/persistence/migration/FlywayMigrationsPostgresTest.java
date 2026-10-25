package com.esep.persistence.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Применяет миграции Flyway к одноразовой пустой PostgreSQL. Пропускается, если Docker недоступен.
 */
@Testcontainers(disabledWithoutDocker = true)
class FlywayMigrationsPostgresTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        dataSource = new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbc = new JdbcTemplate(dataSource);
        flyway(null).clean();
    }

    @Test
    void shouldApplyAllMigrationsToEmptyDatabase() {
        flyway(null).migrate();

        assertThat(flyway(null).info().pending()).isEmpty();
        assertThat(count("select count(*) from merchants where normalized_name = 'ПЕРЕВОДЫ ЛЮДЯМ'")).isEqualTo(1);
        assertThat(count("select count(*) from categories where code = 'UNCATEGORIZED'")).isEqualTo(1);
    }

    @Test
    void shouldRemovePrivatePersonNamesAndKeepOrganizations() {
        flyway("10").migrate();
        long organizationsBefore = count("select count(*) from merchants where merchant_type <> 'PERSON'");
        long organizationAliasesBefore = count("""
                select count(*) from merchant_aliases alias join merchants merchant on merchant.id = alias.merchant_id
                where merchant.merchant_type <> 'PERSON'""");
        insertSyntheticPersonWithTransfer();

        flyway(null).migrate();

        assertThat(count("""
                select count(*) from merchant_aliases alias join merchants merchant on merchant.id = alias.merchant_id
                where merchant.merchant_type = 'PERSON'""")).isZero();
        assertThat(jdbc.queryForList("select normalized_name from merchants where merchant_type = 'PERSON'", String.class))
                .containsExactly("ПЕРЕВОДЫ ЛЮДЯМ");
        assertThat(jdbc.queryForObject("""
                select merchant.normalized_name from transactions transaction_row
                join merchants merchant on merchant.id = transaction_row.merchant_id
                where transaction_row.source_transaction_fingerprint = repeat('b', 64)""", String.class))
                .isEqualTo("ПЕРЕВОДЫ ЛЮДЯМ");
        assertThat(count("select count(*) from merchants where merchant_type <> 'PERSON'")).isEqualTo(organizationsBefore);
        assertThat(count("""
                select count(*) from merchant_aliases alias join merchants merchant on merchant.id = alias.merchant_id
                where merchant.merchant_type <> 'PERSON'""")).isEqualTo(organizationAliasesBefore);
    }

    private void insertSyntheticPersonWithTransfer() {
        jdbc.update("""
                insert into statements (bank_name, original_file_name, source_file_hash, uploaded_at, period_from, period_to)
                values ('KASPI', 'synthetic.pdf', repeat('a', 64), now(), date '2026-08-01', date '2026-08-31')""");
        jdbc.update("""
                insert into merchants (original_name, normalized_name, category_id, merchant_type, confidence,
                                       detection_source, verified, created_at, updated_at)
                values ('Тестов Т.', 'ТЕСТОВ Т', (select id from categories where code = 'PERSONAL_TRANSFERS'),
                        'PERSON', 1.00, 'MANUAL', true, now(), now())""");
        jdbc.update("""
                insert into merchant_aliases (alias_name, normalized_alias, merchant_id, created_at, verified)
                values ('Тестов Тест', 'ТЕСТОВ ТЕСТ', (select id from merchants where normalized_name = 'ТЕСТОВ Т'), now(), true)""");
        jdbc.update("""
                insert into transactions (amount, currency, transaction_date, source_transaction_fingerprint, description,
                                          transaction_type, bank_operation_type, merchant_id, statement_id)
                values (-1000, 'KZT', date '2026-08-02', repeat('b', 64), 'Тестов Т.', 'TRANSFER', 'TRANSFER',
                        (select id from merchants where normalized_name = 'ТЕСТОВ Т'), (select id from statements))""");
    }

    private Flyway flyway(String target) {
        var configuration = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .cleanDisabled(false);
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private long count(String sql) {
        Long value = jdbc.queryForObject(sql, Long.class);
        return value == null ? 0 : value;
    }
}
