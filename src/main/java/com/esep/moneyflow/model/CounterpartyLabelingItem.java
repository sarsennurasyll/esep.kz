package com.esep.moneyflow.model;

import java.math.BigDecimal;

/**
 * Контрагент, назначение операций с которым может указать пользователь.
 *
 * @param labeledAmount сумма операций, у которых назначение уже известно (по правилу или вручную)
 * @param rule          правило пользователя или {@code null}
 */
public record CounterpartyLabelingItem(
        String counterpartyKey,
        String exampleDescription,
        MoneyDirection direction,
        BigDecimal amount,
        long operationCount,
        BigDecimal labeledAmount,
        CounterpartyPurposeRule rule
) {
}
