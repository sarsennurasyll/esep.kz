package com.esep.analytics.service;

import com.esep.analytics.interfaces.AnalyticsQuery;
import com.esep.analytics.interfaces.AnalyticsService;
import com.esep.analytics.model.AnalyticsSummary;
import com.esep.analytics.model.CategoryExpense;
import com.esep.analytics.model.MerchantExpense;
import com.esep.analytics.model.MonthlyAnalytics;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Предоставляет агрегированную аналитику без загрузки операций в память.
 */
@Service
public class DefaultAnalyticsService implements AnalyticsService {

    private final AnalyticsQuery analyticsQuery;

    public DefaultAnalyticsService(AnalyticsQuery analyticsQuery) {
        this.analyticsQuery = analyticsQuery;
    }

    @Override
    public AnalyticsSummary getSummary() {
        return analyticsQuery.getSummary();
    }

    @Override
    public List<CategoryExpense> getCategoryExpenses() {
        List<CategoryExpense> expenses = analyticsQuery.getCategoryExpenses();
        BigDecimal totalExpense = expenses.stream()
                .map(CategoryExpense::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalExpense.signum() == 0) {
            return expenses.stream()
                    .map(expense -> new CategoryExpense(expense.category(), expense.amount(), BigDecimal.ZERO))
                    .toList();
        }

        return expenses.stream()
                .map(expense -> new CategoryExpense(
                        expense.category(),
                        expense.amount(),
                        expense.amount()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalExpense, 2, RoundingMode.HALF_UP)
                ))
                .toList();
    }

    @Override
    public List<MerchantExpense> getTopMerchants() {
        return analyticsQuery.getTopMerchants();
    }

    @Override
    public List<MonthlyAnalytics> getMonthlyAnalytics() {
        return analyticsQuery.getMonthlyAnalytics();
    }
}
