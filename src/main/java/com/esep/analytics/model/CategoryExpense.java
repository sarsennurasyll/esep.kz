package com.esep.analytics.model;

import java.math.BigDecimal;

/**
 * Расходы по категории.
 */
public record CategoryExpense(
        String category,
        BigDecimal amount,
        BigDecimal percent
) {
}
