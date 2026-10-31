package com.esep.account.service;

import com.esep.security.LoginAttemptLimiter;
import com.esep.security.AccountPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Загружает учётную запись для входа по адресу почты. Заблокированный после подбора пароля адрес
 * отклоняется до проверки пароля.
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountService accountService;
    private final LoginAttemptLimiter loginAttemptLimiter;
    private final Set<String> adminEmails;

    public AccountUserDetailsService(
            AccountService accountService,
            LoginAttemptLimiter loginAttemptLimiter,
            @Value("${esep.security.admin-emails:}") List<String> adminEmails
    ) {
        this.adminEmails = adminEmails.stream().flatMap(email -> AccountService.normalizeEmail(email).stream()).collect(Collectors.toSet());
        this.accountService = accountService;
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return accountService.findByEmail(username)
                .map(account -> new AccountPrincipal(
                        account.getId(),
                        account.getEmail(),
                        account.getPasswordHash(),
                        loginAttemptLimiter.isLocked(account.getEmail()),
                        adminEmails.contains(account.getEmail())
                                ? AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")
                                : AuthorityUtils.createAuthorityList("ROLE_USER")))
                .orElseThrow(() -> new UsernameNotFoundException("Account was not found"));
    }
}
