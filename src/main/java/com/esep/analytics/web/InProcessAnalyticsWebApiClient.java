package com.esep.analytics.web;

import com.esep.analytics.api.AnalyticsController;
import com.esep.analytics.api.dto.AnalyticsSummaryResponse;
import com.esep.analytics.api.dto.CategoryExpenseResponse;
import com.esep.analytics.api.dto.CategoryOperationCountResponse;
import com.esep.analytics.api.dto.MerchantExpenseResponse;
import com.esep.analytics.api.dto.MerchantTypeExpenseResponse;
import com.esep.analytics.api.dto.MoneyFlowReportResponse;
import com.esep.analytics.api.dto.MonthlyMoneyFlowResponse;
import com.esep.analytics.api.dto.MonthlyAnalyticsResponse;
import com.esep.analytics.api.dto.PersonTransferResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Получает аналитику через API в том же процессе, без HTTP-запроса к самому приложению.
 */
@Component
public class InProcessAnalyticsWebApiClient implements AnalyticsWebApiClient {

    private final AnalyticsController analyticsController;

    public InProcessAnalyticsWebApiClient(AnalyticsController analyticsController) {
        this.analyticsController = analyticsController;
    }

    @Override
    public AnalyticsSummaryResponse getSummary() {
        return analyticsController.getSummary();
    }

    @Override
    public List<CategoryExpenseResponse> getCategoryExpenses() {
        return analyticsController.getCategoryExpenses();
    }

    @Override
    public List<CategoryOperationCountResponse> getCategoryOperationCounts() {
        return analyticsController.getCategoryOperationCounts();
    }

    @Override
    public List<MerchantExpenseResponse> getTopMerchants() {
        return analyticsController.getTopMerchants();
    }

    @Override
    public List<MerchantTypeExpenseResponse> getMerchantTypeExpenses() {
        return analyticsController.getMerchantTypeExpenses();
    }

    @Override
    public List<PersonTransferResponse> getTopPersonTransfers() {
        return analyticsController.getTopPersonTransfers();
    }

    @Override
    public List<MonthlyAnalyticsResponse> getMonthlyAnalytics() {
        return analyticsController.getMonthlyAnalytics();
    }

    @Override
    public MoneyFlowReportResponse getMoneyFlowReport() {
        return analyticsController.getMoneyFlowReport();
    }

    @Override
    public List<MonthlyMoneyFlowResponse> getMonthlyMoneyFlowReports() {
        return analyticsController.getMonthlyMoneyFlowReports();
    }
}
