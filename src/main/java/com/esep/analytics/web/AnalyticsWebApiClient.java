package com.esep.analytics.web;

import com.esep.analytics.api.dto.AnalyticsSummaryResponse;
import com.esep.analytics.api.dto.CategoryOperationCountResponse;
import com.esep.analytics.api.dto.CategoryExpenseResponse;
import com.esep.analytics.api.dto.MerchantExpenseResponse;
import com.esep.analytics.api.dto.MerchantTypeExpenseResponse;
import com.esep.analytics.api.dto.MonthlyAnalyticsResponse;
import com.esep.analytics.api.dto.PersonTransferResponse;

import java.util.List;

/**
 * Клиент HTTP API аналитики для web-интерфейса.
 */
public interface AnalyticsWebApiClient {

    AnalyticsSummaryResponse getSummary();

    List<CategoryExpenseResponse> getCategoryExpenses();

    List<CategoryOperationCountResponse> getCategoryOperationCounts();

    List<MerchantExpenseResponse> getTopMerchants();

    List<MerchantTypeExpenseResponse> getMerchantTypeExpenses();

    List<PersonTransferResponse> getTopPersonTransfers();

    List<MonthlyAnalyticsResponse> getMonthlyAnalytics();
}
