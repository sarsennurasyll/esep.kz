package com.esep.persistence.migration;

import com.esep.security.AccountPrincipal;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Входит тестовым владельцем до открытия транзакции теста: Hibernate определяет владельца при создании сессии.
 */
class TestOwnerListener implements TestExecutionListener, Ordered {

    static final long OWNER_ID = 1L;

    @Override
    public int getOrder() {
        return 1000;
    }

    @Override
    public void beforeTestMethod(TestContext context) {
        AccountPrincipal principal = new AccountPrincipal(OWNER_ID, "owner@example.test", "unused", false, AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Override
    public void afterTestMethod(TestContext context) {
        SecurityContextHolder.clearContext();
    }

    /** Создаёт учётную запись и делает её владельцем строк, которые тест вставляет обычным SQL. */
    static void prepareOwner(JdbcTemplate jdbc) {
        jdbc.update("insert into user_accounts (id, email, password_hash, created_at) values (?, 'owner@example.test', 'unused', now())", OWNER_ID);
        jdbc.execute("alter table statements alter column owner_id set default " + OWNER_ID);
        jdbc.execute("alter table transactions alter column owner_id set default " + OWNER_ID);
    }
}
