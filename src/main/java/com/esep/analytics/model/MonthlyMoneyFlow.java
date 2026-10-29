package com.esep.analytics.model;

import com.esep.moneyflow.model.MoneyFlowReport;

import java.time.YearMonth;

/**
 * Движение денег за месяц.
 *
 * @param complete {@code true}, если импортированные выписки покрывают каждый день месяца;
 *                 иначе суммы месяца неполные и их нельзя сравнивать с полными месяцами
 */
public record MonthlyMoneyFlow(YearMonth month, MoneyFlowReport report, boolean complete) {
}
