package com.esep.analytics.model;

/**
 * Количество расходных операций по категории.
 */
public record CategoryOperationCount(
        String category,
        String categoryName,
        long transactionCount
) {
}
