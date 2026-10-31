package com.esep.security;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Сообщает Hibernate владельца личных данных (ADR-0009): все запросы к сущностям с {@code @TenantId}
 * получают условие по этому значению, а новые строки — это значение.
 *
 * <p>Без вошедшего пользователя возвращается несуществующий владелец: запросы не находят ни одной строки,
 * а запись отклоняет внешний ключ. Hibernate создаёт этот класс сам, поэтому контекст читается статически.
 */
public class CurrentOwnerResolver implements CurrentTenantIdentifierResolver<Long> {

    static final long NO_OWNER = -1L;

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof AccountPrincipal principal
                ? principal.accountId()
                : NO_OWNER;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
