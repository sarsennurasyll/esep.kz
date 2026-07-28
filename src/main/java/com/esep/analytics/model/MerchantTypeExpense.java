package com.esep.analytics.model;

import com.esep.entity.MerchantType;

import java.math.BigDecimal;

/**
 * Расходы по типу получателя платежа.
 */
public record MerchantTypeExpense(
        MerchantType merchantType,
        BigDecimal amount,
        long transactionCount
) {
}
