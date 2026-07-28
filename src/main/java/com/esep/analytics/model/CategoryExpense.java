package com.esep.analytics.model;

import java.math.BigDecimal;

/**
 * Расходы по категории.
 */
public record CategoryExpense(
        String category,
        String categoryName,
        BigDecimal amount,
        BigDecimal percent
) {
}
