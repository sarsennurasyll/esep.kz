package com.esep.persistence.jpa.read.repository;

import java.math.BigDecimal;

/**
 * Проекция сводных агрегатов операций.
 */
public interface AnalyticsSummaryProjection {

    long getTotalTransactions();

    BigDecimal getTotalIncome();

    BigDecimal getTotalExpense();

    long getRecognizedTransactions();

    long getUnknownTransactions();
}
