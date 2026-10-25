package com.esep.moneyflow.model;

import java.util.Objects;

/**
 * Личное правило пользователя: операции с этим контрагентом в этом направлении имеют указанное назначение.
 * Хранится отдельно от общей базы продавцов.
 */
public record CounterpartyPurposeRule(
        long id,
        String counterpartyKey,
        MoneyDirection direction,
        MoneyPurpose purpose
) {

    public CounterpartyPurposeRule {
        Objects.requireNonNull(counterpartyKey, "Counterparty key must not be null");
        Objects.requireNonNull(direction, "Direction must not be null");
        Objects.requireNonNull(purpose, "Purpose must not be null");
    }
}
