package com.esep.persistence.migration;

import com.esep.entity.BankType;
import com.esep.persistence.jpa.adapter.StatementCatalogJpaAdapter;
import com.esep.persistence.jpa.mapper.StatementJpaMapper;
import com.esep.persistence.model.StatementPersistenceCommand;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Проверяет, что гонка при загрузке одного файла упирается в уникальный индекс из миграций и
 * сообщается как «уже импортирована». Пропускается без Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({StatementCatalogJpaAdapter.class, StatementJpaMapper.class})
@TestPropertySource(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
@Testcontainers(disabledWithoutDocker = true)
@TestExecutionListeners(listeners = TestOwnerListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class StatementCatalogPostgresIntegrationTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private StatementCatalogJpaAdapter adapter;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void prepareOwner() {
        TestOwnerListener.prepareOwner(jdbc);
    }

    @Test
    void shouldReportDuplicateFileHashAsAlreadyImported() {
        // Выписка, сохранённая параллельным запросом после того, как этот запрос уже проверил хеш.
        jdbc.update("""
                insert into statements (bank_name, original_file_name, source_file_hash, uploaded_at, period_from, period_to)
                values ('KASPI', 'synthetic.pdf', repeat('f', 64), now(), date '2026-08-01', date '2026-08-31')""");

        assertThatThrownBy(() -> adapter.save(new StatementPersistenceCommand(
                BankType.KASPI, "synthetic.pdf", "****0000", "f".repeat(64), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)
        ))).isInstanceOf(StatementAlreadyImportedException.class);
    }
}
