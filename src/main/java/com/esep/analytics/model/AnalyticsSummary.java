package com.esep.analytics.model;

import java.math.BigDecimal;

/**
 * Сводные показатели импортированных банковских операций.
 */
public record AnalyticsSummary(
        long totalStatements,
        long totalTransactions,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        long recognizedTransactions,
        long unknownTransactions
) {
}
