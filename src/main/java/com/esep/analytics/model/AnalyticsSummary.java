package com.esep.analytics.model;

import java.math.BigDecimal;

/**
 * Сводные показатели импортированных банковских операций.
 *
 * @param purchasesWithMerchant покупки и возвраты покупок, у которых найден продавец
 * @param purchasesWithoutMerchant покупки и возвраты покупок без продавца; переводы и наличные сюда не входят
 */
public record AnalyticsSummary(
        long totalStatements,
        long totalTransactions,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        long purchasesWithMerchant,
        long purchasesWithoutMerchant
) {
}
