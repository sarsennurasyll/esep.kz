package com.esep.account.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Удаляет учётную запись и все её личные данные одной транзакцией. Общая база продавцов не меняется.
 */
@Service
public class AccountDeletionService {

    private final JdbcTemplate jdbcTemplate;

    public AccountDeletionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void delete(long accountId) {
        jdbcTemplate.update("delete from transactions where owner_id = ?", accountId);
        jdbcTemplate.update("delete from statements where owner_id = ?", accountId);
        jdbcTemplate.update("delete from counterparty_purpose_rules where owner_id = ?", accountId);
        jdbcTemplate.update("delete from user_accounts where id = ?", accountId);
    }
}
