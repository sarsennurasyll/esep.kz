package com.esep.analytics.model;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Доходы и расходы за календарный месяц.
 */
public record MonthlyAnalytics(
        YearMonth month,
        BigDecimal income,
        BigDecimal expense
) {
}
