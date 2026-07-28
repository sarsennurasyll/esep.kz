package com.esep.analytics.api.dto;

import com.esep.analytics.model.MonthlyAnalytics;

import java.math.BigDecimal;

/**
 * HTTP-представление помесячной аналитики.
 */
public record MonthlyAnalyticsResponse(
        String month,
        BigDecimal income,
        BigDecimal expense
) {

    public static MonthlyAnalyticsResponse from(MonthlyAnalytics analytics) {
        return new MonthlyAnalyticsResponse(
                analytics.month().toString(),
                analytics.income(),
                analytics.expense()
        );
    }
}
