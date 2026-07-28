package com.esep.analytics.api.dto;

import com.esep.analytics.model.MerchantTypeExpense;

import java.math.BigDecimal;

/**
 * HTTP-представление расходов по типу получателя.
 */
public record MerchantTypeExpenseResponse(
        String merchantType,
        String merchantTypeName,
        BigDecimal amount,
        long transactionCount
) {

    public static MerchantTypeExpenseResponse from(MerchantTypeExpense expense) {
        return new MerchantTypeExpenseResponse(
                expense.merchantType().name(),
                expense.merchantType().displayName(),
                expense.amount(),
                expense.transactionCount()
        );
    }
}
