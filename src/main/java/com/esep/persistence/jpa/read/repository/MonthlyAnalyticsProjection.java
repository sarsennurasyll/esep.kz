package com.esep.persistence.jpa.read.repository;

import java.math.BigDecimal;

/**
 * Проекция доходов и расходов за календарный месяц.
 */
public interface MonthlyAnalyticsProjection {

    int getYear();

    int getMonth();

    BigDecimal getIncome();

    BigDecimal getExpense();
}
