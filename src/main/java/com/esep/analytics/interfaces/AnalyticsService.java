package com.esep.analytics.interfaces;

import com.esep.analytics.model.AnalyticsSummary;
import com.esep.analytics.model.CategoryOperationCount;
import com.esep.analytics.model.CategoryExpense;
import com.esep.analytics.model.MerchantExpense;
import com.esep.analytics.model.MerchantTypeExpense;
import com.esep.analytics.model.MonthlyAnalytics;
import com.esep.analytics.model.PersonTransfer;

import java.util.List;

/**
 * Query-сервис аналитики для API и web-слоя.
 */
public interface AnalyticsService {

    AnalyticsSummary getSummary();

    List<CategoryExpense> getCategoryExpenses();

    List<CategoryOperationCount> getCategoryOperationCounts();

    List<MerchantExpense> getTopMerchants();

    List<MerchantTypeExpense> getMerchantTypeExpenses();

    List<PersonTransfer> getTopPersonTransfers();

    List<MonthlyAnalytics> getMonthlyAnalytics();
}
