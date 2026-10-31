package com.esep.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

/**
 * Передаёт данные без владельца, созданные до ADR-0009, учётной записи из настройки. Без настройки ничего не делает:
 * такие данные никому не видны, а автоматически отдавать их первому зарегистрированному небезопасно.
 */
@Component
public class LegacyDataClaimer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyDataClaimer.class);

    private final AccountService accountService;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final String ownerEmail;

    public LegacyDataClaimer(
            AccountService accountService,
            JdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate,
            @Value("${esep.security.legacy-data-owner-email:}") String ownerEmail
    ) {
        this.accountService = accountService;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.ownerEmail = ownerEmail;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (ownerEmail.isBlank()) {
            return;
        }
        Optional<Long> owner = accountService.findByEmail(ownerEmail).map(account -> account.getId());
        if (owner.isEmpty()) {
            log.warn("Legacy data owner account was not found; legacy data stays unassigned");
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {
            // Сначала выписки: составной внешний ключ требует, чтобы у выписки уже был тот же владелец, что у операции.
            int statements = jdbcTemplate.update("update statements set owner_id = ? where owner_id is null", owner.get());
            jdbcTemplate.update("update transactions set owner_id = ? where owner_id is null", owner.get());
            int rules = jdbcTemplate.update("update counterparty_purpose_rules set owner_id = ? where owner_id is null", owner.get());
            log.info("Legacy data assigned: statements={}, rules={}", statements, rules);
        });
    }
}
