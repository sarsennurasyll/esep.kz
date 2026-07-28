package com.esep.analytics.web;

import com.esep.analytics.api.dto.AnalyticsSummaryResponse;
import com.esep.analytics.api.dto.CategoryExpenseResponse;
import com.esep.analytics.api.dto.MerchantExpenseResponse;
import com.esep.analytics.api.dto.MonthlyAnalyticsResponse;

import java.util.List;

/**
 * Клиент HTTP API аналитики для web-интерфейса.
 */
public interface AnalyticsWebApiClient {

    AnalyticsSummaryResponse getSummary();

    List<CategoryExpenseResponse> getCategoryExpenses();

    List<MerchantExpenseResponse> getTopMerchants();

    List<MonthlyAnalyticsResponse> getMonthlyAnalytics();
}
