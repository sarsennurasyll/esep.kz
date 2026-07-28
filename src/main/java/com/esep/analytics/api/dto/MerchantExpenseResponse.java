package com.esep.analytics.api.dto;

import com.esep.analytics.model.MerchantExpense;

import java.math.BigDecimal;

/**
 * HTTP-представление расходов у продавца.
 */
public record MerchantExpenseResponse(
        String merchant,
        BigDecimal amount,
        long transactionCount
) {

    public static MerchantExpenseResponse from(MerchantExpense expense) {
        return new MerchantExpenseResponse(expense.merchant(), expense.amount(), expense.transactionCount());
    }
}
