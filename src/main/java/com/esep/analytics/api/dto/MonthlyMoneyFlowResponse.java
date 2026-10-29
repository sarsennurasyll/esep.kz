package com.esep.analytics.api.dto;

import com.esep.analytics.model.MonthlyMoneyFlow;

/**
 * HTTP-представление отчёта о движении денег за месяц.
 *
 * @param month    месяц в формате {@code yyyy-MM}
 * @param complete {@code true}, если выписки покрывают каждый день месяца
 */
public record MonthlyMoneyFlowResponse(String month, MoneyFlowReportResponse report, boolean complete) {

    public static MonthlyMoneyFlowResponse from(MonthlyMoneyFlow monthly) {
        return new MonthlyMoneyFlowResponse(monthly.month().toString(), MoneyFlowReportResponse.from(monthly.report()), monthly.complete());
    }
}
