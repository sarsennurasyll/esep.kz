package com.esep.analytics.interfaces;

import com.esep.analytics.model.AnalyticsSummary;
import com.esep.analytics.model.CategoryExpense;
import com.esep.analytics.model.MerchantExpense;
import com.esep.analytics.model.MonthlyAnalytics;

import java.util.List;

/**
 * Read-порт агрегированной аналитики импортированных операций.
 */
public interface AnalyticsQuery {

    AnalyticsSummary getSummary();

    List<CategoryExpense> getCategoryExpenses();

    List<MerchantExpense> getTopMerchants();

    List<MonthlyAnalytics> getMonthlyAnalytics();
}
