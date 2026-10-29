package com.esep.account.service;

import com.esep.security.LoginAttemptLimiter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Загружает учётную запись для входа по адресу почты. Заблокированный после подбора пароля адрес
 * отклоняется до проверки пароля.
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountService accountService;
    private final LoginAttemptLimiter loginAttemptLimiter;

    public AccountUserDetailsService(AccountService accountService, LoginAttemptLimiter loginAttemptLimiter) {
        this.accountService = accountService;
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return accountService.findByEmail(username)
                .map(account -> User.withUsername(account.getEmail())
                        .password(account.getPasswordHash())
                        .roles("USER")
                        .accountLocked(loginAttemptLimiter.isLocked(account.getEmail()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Account was not found"));
    }
}
