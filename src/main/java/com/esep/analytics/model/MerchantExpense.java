package com.esep.analytics.model;

import java.math.BigDecimal;

/**
 * Агрегированные расходы у продавца.
 */
public record MerchantExpense(
        String merchant,
        BigDecimal amount,
        long transactionCount
) {
}
