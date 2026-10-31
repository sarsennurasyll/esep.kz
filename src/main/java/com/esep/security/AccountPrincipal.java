package com.esep.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Вошедший пользователь вместе с идентификатором учётной записи, по которому разделяются личные данные.
 */
public class AccountPrincipal extends User {

    private final long accountId;

    public AccountPrincipal(long accountId, String email, String passwordHash, boolean locked, Collection<? extends GrantedAuthority> authorities) {
        super(email, passwordHash, true, true, true, !locked, authorities);
        this.accountId = accountId;
    }

    public long accountId() {
        return accountId;
    }
}
