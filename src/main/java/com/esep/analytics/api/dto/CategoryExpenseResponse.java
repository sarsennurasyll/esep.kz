package com.esep.analytics.api.dto;

import com.esep.analytics.model.CategoryExpense;

import java.math.BigDecimal;

/**
 * HTTP-представление расходов по категории.
 */
public record CategoryExpenseResponse(
        String category,
        BigDecimal amount,
        BigDecimal percent
) {

    public static CategoryExpenseResponse from(CategoryExpense expense) {
        return new CategoryExpenseResponse(expense.category(), expense.amount(), expense.percent());
    }
}
