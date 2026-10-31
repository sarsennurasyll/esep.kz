package com.esep.persistence.migration;

import com.esep.account.service.AccountRegistrationService;
import com.esep.account.service.AccountService;
import com.esep.account.service.ConsentService;
import com.esep.entity.LegalDocument;
import com.esep.merchantmanagement.service.MerchantSuggestionService;
import com.esep.security.AccountPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Согласия с версиями и пополнение общей базы по отдельному согласию (ADR-0010).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
@Testcontainers(disabledWithoutDocker = true)
class ConsentPostgresIntegrationTest {

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
    private AccountRegistrationService registrationService;
    @Autowired
    private ConsentService consentService;
    @Autowired
    private MerchantSuggestionService suggestionService;

    @Test
    void shouldRecordVersionsOnRegistrationWithContributionOffByDefault() throws Exception {
        String email = email();
        mockMvc.perform(post("/register").with(csrf()).param("email", email).param("password", PASSWORD)
                        .param("passwordConfirmation", PASSWORD).param("consent", "true"))
                .andExpect(redirectedUrl("/login"));

        long id = accountService.findByEmail(email).orElseThrow().getId();
        assertThat(jdbc.queryForList("select document || ':' || version from user_consents where account_id = ? and granted order by document", String.class, id))
                .containsExactly("PRIVACY:" + LegalDocument.PRIVACY.version(), "TERMS:" + LegalDocument.TERMS.version());
        assertThat(consentService.isContributing(id)).isFalse();
    }

    @Test
    void shouldBlockFullAnalysisUntilCurrentVersionsAreAccepted() throws Exception {
        String email = email();
        long id = accountService.register(email, PASSWORD);

        mockMvc.perform(get("/statements").with(as(id))).andExpect(redirectedUrl("/consent"));
        mockMvc.perform(get("/api/statements").with(as(id))).andExpect(status().isForbidden());
        mockMvc.perform(get("/account").with(as(id, email))).andExpect(status().isOk());
        mockMvc.perform(get("/consent").with(as(id))).andExpect(status().isOk());
        mockMvc.perform(post("/consent").with(as(id)).with(csrf())).andExpect(status().isBadRequest());
        mockMvc.perform(get("/statements").with(as(id))).andExpect(redirectedUrl("/consent"));

        mockMvc.perform(post("/consent").with(as(id)).with(csrf()).param("accept", "true")).andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/statements").with(as(id))).andExpect(status().isOk());
        mockMvc.perform(get("/api/statements").with(as(id))).andExpect(status().isOk());
    }

    @Test
    void shouldAskAgainWhenAcceptedVersionIsOutdated() throws Exception {
        long id = registrationService.register(email(), PASSWORD, false);
        jdbc.update("""
                insert into user_consents (account_id, document, version, granted, recorded_at)
                values (?, 'TERMS', 'old', true, now())""", id);

        assertThat(consentService.outstanding(id)).containsExactly(LegalDocument.TERMS);
        mockMvc.perform(get("/statements").with(as(id))).andExpect(redirectedUrl("/consent"));
    }

    @Test
    void shouldServeLegalDocumentsWithoutLogin() throws Exception {
        mockMvc.perform(get("/terms")).andExpect(status().isOk()).andExpect(content().string(containsString(LegalDocument.TERMS.version())));
        mockMvc.perform(get("/privacy")).andExpect(status().isOk()).andExpect(content().string(containsString(LegalDocument.PRIVACY.version())));
    }

    @Test
    void shouldContributeOnlyPurchaseNamesWithConsentAndStopAfterWithdrawal() throws Exception {
        String marker = marker();
        long id = registrationService.register(email(), PASSWORD, false);
        long statement = statement(id);
        operation(id, statement, marker + " SHOP", "PURCHASE");
        operation(id, statement, marker + " TRANSFER", "TRANSFER");
        operation(id, statement, "Тестов Т.", "PURCHASE");

        mockMvc.perform(post("/account/contribution").with(as(id)).with(csrf()).param("enabled", "false")).andExpect(redirectedUrl("/account"));
        assertThat(contributed(marker)).isEmpty();

        mockMvc.perform(post("/account/contribution").with(as(id)).with(csrf()).param("enabled", "true")).andExpect(redirectedUrl("/account"));
        assertThat(contributed(marker)).containsExactly(marker + " SHOP");
        assertThat(jdbc.queryForObject("select count(*) from merchant_suggestions where normalized_name ilike '%тестов%'", Long.class)).isZero();

        mockMvc.perform(post("/account/contribution").with(as(id)).with(csrf()).param("enabled", "false")).andExpect(redirectedUrl("/account"));
        operation(id, statement, marker + " LATER", "PURCHASE");
        mockMvc.perform(post("/account/contribution").with(as(id)).with(csrf()).param("enabled", "false")).andExpect(redirectedUrl("/account"));
        assertThat(contributed(marker)).containsExactly(marker + " SHOP");
    }

    @Test
    void shouldContributeAfterImportOnlyForConsentingCurrentOwner() {
        String marker = marker();
        long id = registrationService.register(email(), PASSWORD, true);
        operation(id, statement(id), marker + " ARENA", "PURCHASE");

        suggestionService.statementImported();
        assertThat(contributed(marker)).isEmpty();

        AccountPrincipal principal = new AccountPrincipal(id, "user@example.test", "unused", false, AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        try {
            suggestionService.statementImported();
        } finally {
            SecurityContextHolder.clearContext();
        }
        assertThat(contributed(marker)).containsExactly(marker + " ARENA");
    }

    @Test
    void shouldStoreOnlyTheNameInSuggestions() {
        assertThat(jdbc.queryForList(
                "select column_name from information_schema.columns where table_name = 'merchant_suggestions' order by column_name", String.class))
                .containsExactly("normalized_name", "status");
    }

    @Test
    void shouldShowSuggestionsOnlyToAdminAndKeepRejectedOnesRejected() throws Exception {
        String marker = marker();
        long id = registrationService.register(email(), PASSWORD, true);
        operation(id, statement(id), marker + " CLUB", "PURCHASE");
        contribute(id);

        mockMvc.perform(get("/knowledge-base/suggestions").with(as(id))).andExpect(status().isForbidden());
        mockMvc.perform(get("/knowledge-base/suggestions").with(admin(id))).andExpect(status().isOk())
                .andExpect(content().string(containsString(marker + " CLUB")));

        mockMvc.perform(post("/knowledge-base/suggestions/reject").with(admin(id)).with(csrf()).param("name", marker + " CLUB"))
                .andExpect(redirectedUrl("/knowledge-base/suggestions"));
        contribute(id);

        assertThat(suggestionService.findNew()).doesNotContain(marker + " CLUB");
    }

    @Test
    void shouldDeleteConsentsWithAccountAndKeepUnlinkedNames() throws Exception {
        String email = email();
        long id = registrationService.register(email, PASSWORD, false);

        mockMvc.perform(post("/account/delete").with(as(id, email)).with(csrf()).param("password", PASSWORD)).andExpect(redirectedUrl("/login?deleted"));

        assertThat(jdbc.queryForObject("select count(*) from user_consents where account_id = ?", Long.class, id)).isZero();
    }

    private void contribute(long id) throws Exception {
        mockMvc.perform(post("/account/contribution").with(as(id)).with(csrf()).param("enabled", "true")).andExpect(redirectedUrl("/account"));
    }

    private List<String> contributed(String marker) {
        return jdbc.queryForList("select normalized_name from merchant_suggestions where normalized_name like ?", String.class, marker + "%");
    }

    private long statement(long owner) {
        return jdbc.queryForObject("""
                insert into statements (owner_id, bank_name, original_file_name, source_file_hash, uploaded_at, period_from, period_to, account_number)
                values (?, 'KASPI', 'synthetic.pdf', ?, now(), date '2026-08-01', date '2026-08-31', '****0000') returning id""",
                Long.class, owner, fingerprint());
    }

    private void operation(long owner, long statementId, String description, String operationType) {
        jdbc.update("""
                insert into transactions (owner_id, amount, currency, transaction_date, source_transaction_fingerprint, description,
                                          transaction_type, bank_operation_type, statement_id)
                values (?, -1234, 'KZT', date '2026-08-02', ?, ?, 'EXPENSE', ?, ?)""",
                owner, fingerprint(), description, operationType, statementId);
    }

    private static String fingerprint() {
        return (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "");
    }

    private static String marker() {
        return "SYNTH" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private static String email() {
        return "consent-" + UUID.randomUUID() + "@example.test";
    }

    private RequestPostProcessor as(long id) {
        return as(id, "user" + id + "@example.test");
    }

    private static RequestPostProcessor as(long id, String email) {
        return SecurityMockMvcRequestPostProcessors.user(new AccountPrincipal(id, email, "unused", false, AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    private static RequestPostProcessor admin(long id) {
        return SecurityMockMvcRequestPostProcessors.user(new AccountPrincipal(id, "admin" + id + "@example.test", "unused", false, AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));
    }
}
