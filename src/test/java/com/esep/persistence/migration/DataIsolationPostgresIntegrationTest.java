package com.esep.persistence.migration;

import com.esep.account.service.AccountDeletionService;
import com.esep.account.service.AccountService;
import com.esep.account.service.LegacyDataClaimer;
import com.esep.entity.BankType;
import com.esep.entity.TransactionType;
import com.esep.moneyflow.interfaces.CounterpartyPurposeRuleCatalog;
import com.esep.persistence.interfaces.StatementCatalog;
import com.esep.persistence.interfaces.TransactionCatalog;
import com.esep.persistence.model.StatementPersistenceCommand;
import com.esep.persistence.model.TransactionPersistenceCommand;
import com.esep.security.AccountPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Один пользователь не может прочитать или изменить данные другого (ADR-0009): всё приложение на настоящей схеме.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
@Testcontainers(disabledWithoutDocker = true)
class DataIsolationPostgresIntegrationTest {

    private static final String DESCRIPTION = "Тестов Т.";
    private static final String PASSWORD = "correct horse battery";

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountDeletionService accountDeletionService;
    @Autowired
    private CounterpartyPurposeRuleCatalog ruleCatalog;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private StatementCatalog statementCatalog;
    @Autowired
    private TransactionCatalog transactionCatalog;

    private long alice;
    private long bob;
    private long aliceStatement;
    private long aliceOperation;

    @BeforeEach
    void createAccountsAndData() {
        alice = account("alice-" + UUID.randomUUID() + "@example.test");
        bob = account("bob-" + UUID.randomUUID() + "@example.test");
        aliceStatement = statement(alice, "a".repeat(64));
        aliceOperation = operation(alice, aliceStatement);
    }

    @Test
    void shouldHideOtherOwnersDataFromEveryRead() throws Exception {
        mockMvc.perform(get("/api/statements/{id}/transactions", aliceStatement).with(as(alice)))
                .andExpect(status().isOk()).andExpect(content().string(containsString(DESCRIPTION)));

        mockMvc.perform(get("/api/statements/{id}", aliceStatement).with(as(bob))).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/statements/{id}/transactions", aliceStatement).with(as(bob)))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mockMvc.perform(get("/api/statements/{id}/deletion", aliceStatement).with(as(bob))).andExpect(status().isNotFound());
        mockMvc.perform(get("/statements/{id}", aliceStatement).with(as(bob))).andExpect(status().isNotFound());
        for (String list : new String[]{"/api/statements", "/api/purposes/counterparties", "/api/merchants/unknown", "/api/analytics/people"}) {
            mockMvc.perform(get(list).with(as(bob)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString(DESCRIPTION))))
                    .andExpect(content().string(not(containsString("\"id\":" + aliceStatement))));
        }
        mockMvc.perform(get("/api/analytics/summary").with(as(bob)))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("1234.00"))));
    }

    @Test
    void shouldRejectChangesToOtherOwnersData() throws Exception {
        mockMvc.perform(delete("/api/statements/{id}", aliceStatement).param("statementIds", String.valueOf(aliceStatement)).with(as(bob)))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/purposes/operations/{id}", aliceOperation).with(as(bob))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"purpose\":\"GIFT\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/purposes/operations/{id}", aliceOperation).with(as(bob))).andExpect(status().isNotFound());

        assertThat(jdbc.queryForObject("select purpose from transactions where id = ?", String.class, aliceOperation)).isEqualTo("UNKNOWN");
        assertThat(jdbc.queryForObject("select count(*) from statements where id = ?", Long.class, aliceStatement)).isEqualTo(1);
    }

    @Test
    void shouldKeepPurposeRulesPerOwner() throws Exception {
        String rule = "{\"counterpartyKey\":\"shared-key\",\"direction\":\"OUTFLOW\",\"purpose\":\"GIFT\"}";
        mockMvc.perform(put("/api/purposes/rules").with(as(alice)).contentType(MediaType.APPLICATION_JSON).content(rule)).andExpect(status().isOk());
        mockMvc.perform(put("/api/purposes/rules").with(as(bob)).contentType(MediaType.APPLICATION_JSON).content(rule)).andExpect(status().isOk());
        Long aliceRule = jdbc.queryForObject("select id from counterparty_purpose_rules where owner_id = ?", Long.class, alice);

        // Тот же ответ, что и на несуществующее правило: чужой id ничем не выделяется.
        mockMvc.perform(delete("/api/purposes/rules/{id}", aliceRule).with(as(bob))).andExpect(status().isBadRequest());

        assertThat(jdbc.queryForObject("select count(*) from counterparty_purpose_rules where owner_id in (?, ?)", Long.class, alice, bob)).isEqualTo(2);
    }

    @Test
    void shouldAllowSameFileForDifferentOwnersButNotOperationOfOtherOwner() {
        long bobStatement = statement(bob, "a".repeat(64));

        assertThat(bobStatement).isNotEqualTo(aliceStatement);
        assertThatThrownBy(() -> operation(bob, aliceStatement)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldSaveImportedStatementAndOperationsForCurrentOwner() {
        String hash = "d".repeat(64);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal(bob, "bob@example.test"), null, List.of()));
        try {
            transactionTemplate.executeWithoutResult(status -> {
                statementCatalog.save(new StatementPersistenceCommand(
                        BankType.KASPI, "synthetic.pdf", "****0000", hash, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)));
                transactionCatalog.saveAll(hash, List.of(new TransactionPersistenceCommand(
                        LocalDate.of(2026, 8, 2), "MAGNUM", new BigDecimal("14500.00"), "KZT", TransactionType.EXPENSE, null, "e".repeat(64))));
            });
        } finally {
            SecurityContextHolder.clearContext();
        }

        assertThat(jdbc.queryForObject("select owner_id from statements where source_file_hash = ? and owner_id = ?", Long.class, hash, bob)).isEqualTo(bob);
        assertThat(jdbc.queryForObject("select owner_id from transactions where source_transaction_fingerprint = ?", Long.class, "e".repeat(64))).isEqualTo(bob);
    }

    @Test
    void shouldSeeNothingAndWriteNothingWithoutLogin() {
        assertThat(ruleCatalog.findAll()).isEmpty();
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status ->
                ruleCatalog.save("key", com.esep.moneyflow.model.MoneyDirection.OUTFLOW, com.esep.moneyflow.model.MoneyPurpose.GIFT)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldLetOnlyAdminChangeSharedKnowledgeBase() throws Exception {
        mockMvc.perform(post("/api/knowledge-base/merchants").with(as(bob)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/merchants/match").with(as(bob)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/merchants/match").with(as(bob)).with(csrf())).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/knowledge-base/merchants").with(as(bob))).andExpect(status().isOk());
    }

    @Test
    void shouldDeleteOnlyOwnAccountDataAfterPasswordConfirmation() throws Exception {
        String email = "deleted-" + UUID.randomUUID() + "@example.test";
        accountService.register(email, PASSWORD);
        long owner = accountService.findByEmail(email).orElseThrow().getId();
        operation(owner, statement(owner, "b".repeat(64)));
        ruleInSql(owner);

        mockMvc.perform(post("/account/delete").param("password", "wrong password here").with(as(owner, email)).with(csrf()))
                .andExpect(redirectedUrl("/account"));
        assertThat(accountService.findByEmail(email)).isPresent();

        mockMvc.perform(post("/account/delete").param("password", PASSWORD).with(as(owner, email)).with(csrf()))
                .andExpect(redirectedUrl("/login?deleted"));

        assertThat(accountService.findByEmail(email)).isEmpty();
        assertThat(jdbc.queryForObject("""
                select (select count(*) from statements where owner_id = ?)
                     + (select count(*) from transactions where owner_id = ?)
                     + (select count(*) from counterparty_purpose_rules where owner_id = ?)""", Long.class, owner, owner, owner)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from transactions where id = ?", Long.class, aliceOperation)).isEqualTo(1);
    }

    @Test
    void shouldGiveLegacyDataOnlyToConfiguredAccount() {
        String email = "legacy-" + UUID.randomUUID() + "@example.test";
        accountService.register(email, PASSWORD);
        long owner = accountService.findByEmail(email).orElseThrow().getId();
        long legacyStatement = statement(null, "c".repeat(64));
        long legacyOperation = operation(null, legacyStatement);

        new LegacyDataClaimer(accountService, jdbc, transactionTemplate, "").run(null);
        assertThat(jdbc.queryForObject("select owner_id from statements where id = ?", Long.class, legacyStatement)).isNull();

        new LegacyDataClaimer(accountService, jdbc, transactionTemplate, email).run(null);
        assertThat(jdbc.queryForObject("select owner_id from statements where id = ?", Long.class, legacyStatement)).isEqualTo(owner);
        assertThat(jdbc.queryForObject("select owner_id from transactions where id = ?", Long.class, legacyOperation)).isEqualTo(owner);

        accountDeletionService.delete(owner);
    }

    private long account(String email) {
        accountService.register(email, PASSWORD);
        return accountService.findByEmail(email).orElseThrow().getId();
    }

    private long statement(Long owner, String hash) {
        return jdbc.queryForObject("""
                insert into statements (owner_id, bank_name, original_file_name, source_file_hash, uploaded_at, period_from, period_to, account_number)
                values (?, 'KASPI', 'synthetic.pdf', ?, now(), date '2026-08-01', date '2026-08-31', '****0000') returning id""",
                Long.class, owner, hash);
    }

    private long operation(Long owner, long statementId) {
        return jdbc.queryForObject("""
                insert into transactions (owner_id, amount, currency, transaction_date, source_transaction_fingerprint, description,
                                          transaction_type, bank_operation_type, flow_type, statement_id)
                values (?, -1234, 'KZT', date '2026-08-02', ?, ?, 'EXPENSE', 'TRANSFER', 'TRANSFER_TO_PERSON', ?) returning id""",
                Long.class, owner, UUID.randomUUID().toString().replace("-", "") + "00000000000000000000000000000000", DESCRIPTION, statementId);
    }

    private void ruleInSql(long owner) {
        jdbc.update("""
                insert into counterparty_purpose_rules (owner_id, counterparty_key, direction, purpose, created_at, updated_at)
                values (?, 'key', 'OUTFLOW', 'GIFT', now(), now())""", owner);
    }

    private RequestPostProcessor as(long accountId) {
        return as(accountId, "user" + accountId + "@example.test");
    }

    private static RequestPostProcessor as(long accountId, String email) {
        return SecurityMockMvcRequestPostProcessors.user(principal(accountId, email));
    }

    private static AccountPrincipal principal(long accountId, String email) {
        return new AccountPrincipal(accountId, email, "unused", false, AuthorityUtils.createAuthorityList("ROLE_USER"));
    }
}
