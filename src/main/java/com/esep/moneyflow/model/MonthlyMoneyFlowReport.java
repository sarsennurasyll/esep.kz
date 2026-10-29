package com.esep.moneyflow.model;

import java.time.YearMonth;

/** Отчёт о движении денег за один календарный месяц по тем же правилам, что и общий отчёт. */
public record MonthlyMoneyFlowReport(YearMonth month, MoneyFlowReport report) {
}
