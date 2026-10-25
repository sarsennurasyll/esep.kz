package com.esep.analytics.api.dto;

import com.esep.moneyflow.model.MoneyFlowReport;

import java.math.BigDecimal;

/**
 * HTTP-представление отчёта о движении денег (ADR-0003).
 */
public record MoneyFlowReportResponse(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal cashWithdrawals,
        BigDecimal familyInflow,
        BigDecimal familyOutflow,
        BigDecimal internalInflow,
        BigDecimal internalOutflow,
        BigDecimal unknownInflow,
        BigDecimal unknownOutflow,
        BigDecimal totalInflow,
        BigDecimal totalOutflow,
        BigDecimal unknownInflowShare,
        BigDecimal unknownOutflowShare
) {

    public static MoneyFlowReportResponse from(MoneyFlowReport report) {
        return new MoneyFlowReportResponse(
                report.income(),
                report.expense(),
                report.cashWithdrawals(),
                report.familyInflow(),
                report.familyOutflow(),
                report.internalInflow(),
                report.internalOutflow(),
                report.unknownInflow(),
                report.unknownOutflow(),
                report.totalInflow(),
                report.totalOutflow(),
                report.unknownInflowShare(),
                report.unknownOutflowShare()
        );
    }
}
