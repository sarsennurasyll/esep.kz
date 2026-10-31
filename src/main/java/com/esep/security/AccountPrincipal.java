package com.esep.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Optional;

/**
 * Вошедший пользователь вместе с идентификатором учётной записи, по которому разделяются личные данные.
 */
public class AccountPrincipal extends User {

    private final long accountId;

    public AccountPrincipal(long accountId, String email, String passwordHash, boolean locked, Collection<? extends GrantedAuthority> authorities) {
        super(email, passwordHash, true, true, true, !locked, authorities);
        this.accountId = accountId;
    }

    public static Optional<AccountPrincipal> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof AccountPrincipal principal
                ? Optional.of(principal)
                : Optional.empty();
    }

    public long accountId() {
        return accountId;
    }
}
