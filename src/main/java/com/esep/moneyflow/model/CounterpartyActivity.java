package com.esep.moneyflow.model;

import java.math.BigDecimal;

/**
 * Агрегат операций с одним исходным описанием контрагента, направлением и текущим назначением.
 * Сумма неотрицательная.
 */
public record CounterpartyActivity(
        String description,
        MoneyDirection direction,
        MoneyPurpose purpose,
        BigDecimal amount,
        long operationCount
) {
}
