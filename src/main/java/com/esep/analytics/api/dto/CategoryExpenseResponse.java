package com.esep.analytics.api.dto;

import com.esep.analytics.model.CategoryExpense;

import java.math.BigDecimal;

/**
 * HTTP-представление расходов по категории.
 */
public record CategoryExpenseResponse(
        String category,
        String categoryName,
        BigDecimal amount,
        BigDecimal percent
) {

    public static CategoryExpenseResponse from(CategoryExpense expense) {
        return new CategoryExpenseResponse(
                expense.category(),
                expense.categoryName(),
                expense.amount(),
                expense.percent()
        );
    }
}
