package com.esep.analytics.api.dto;

import com.esep.analytics.model.AnalyticsSummary;

import java.math.BigDecimal;

/**
 * HTTP-представление сводных показателей аналитики.
 */
public record AnalyticsSummaryResponse(
        long totalStatements,
        long totalTransactions,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        long recognizedTransactions,
        long unknownTransactions
) {

    public static AnalyticsSummaryResponse from(AnalyticsSummary summary) {
        return new AnalyticsSummaryResponse(
                summary.totalStatements(),
                summary.totalTransactions(),
                summary.totalIncome(),
                summary.totalExpense(),
                summary.recognizedTransactions(),
                summary.unknownTransactions()
        );
    }
}
