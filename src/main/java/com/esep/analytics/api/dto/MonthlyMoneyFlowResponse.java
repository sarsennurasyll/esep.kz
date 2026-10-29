package com.esep.analytics.api.dto;

import com.esep.moneyflow.model.MonthlyMoneyFlowReport;

/**
 * HTTP-представление отчёта о движении денег за месяц.
 *
 * @param month месяц в формате {@code yyyy-MM}
 */
public record MonthlyMoneyFlowResponse(String month, MoneyFlowReportResponse report) {

    public static MonthlyMoneyFlowResponse from(MonthlyMoneyFlowReport monthlyReport) {
        return new MonthlyMoneyFlowResponse(monthlyReport.month().toString(), MoneyFlowReportResponse.from(monthlyReport.report()));
    }
}
