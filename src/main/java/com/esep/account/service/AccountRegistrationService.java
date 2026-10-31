package com.esep.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Создаёт учётную запись и записывает согласия одной транзакцией: аккаунта без записи о согласии быть не должно.
 */
@Service
public class AccountRegistrationService {

    private final AccountService accountService;
    private final ConsentService consentService;

    public AccountRegistrationService(AccountService accountService, ConsentService consentService) {
        this.accountService = accountService;
        this.consentService = consentService;
    }

    @Transactional
    public long register(String email, String password, boolean merchantContribution) {
        long accountId = accountService.register(email, password);
        consentService.acceptRequired(accountId);
        if (merchantContribution) {
            consentService.setContribution(accountId, true);
        }
        return accountId;
    }
}
