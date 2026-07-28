package com.esep.persistence.jpa.read.adapter;

import com.esep.analytics.interfaces.AnalyticsQuery;
import com.esep.analytics.model.AnalyticsSummary;
import com.esep.analytics.model.CategoryExpense;
import com.esep.analytics.model.MerchantExpense;
import com.esep.analytics.model.MonthlyAnalytics;
import com.esep.persistence.jpa.read.repository.AnalyticsStatementReadJpaRepository;
import com.esep.persistence.jpa.read.repository.AnalyticsTransactionReadJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * JPA-адаптер read-порта аналитики с агрегированием на стороне базы данных.
 */
@Repository
public class AnalyticsQueryJpaAdapter implements AnalyticsQuery {

    private static final int TOP_MERCHANTS_LIMIT = 20;

    private final AnalyticsStatementReadJpaRepository statementReadJpaRepository;
    private final AnalyticsTransactionReadJpaRepository transactionReadJpaRepository;

    public AnalyticsQueryJpaAdapter(
            AnalyticsStatementReadJpaRepository statementReadJpaRepository,
            AnalyticsTransactionReadJpaRepository transactionReadJpaRepository
    ) {
        this.statementReadJpaRepository = statementReadJpaRepository;
        this.transactionReadJpaRepository = transactionReadJpaRepository;
    }

    @Override
    public AnalyticsSummary getSummary() {
        var summary = transactionReadJpaRepository.getSummary();
        return new AnalyticsSummary(
                statementReadJpaRepository.countStatements(),
                summary.getTotalTransactions(),
                zeroIfNull(summary.getTotalIncome()),
                zeroIfNull(summary.getTotalExpense()),
                summary.getRecognizedTransactions(),
                summary.getUnknownTransactions()
        );
    }

    @Override
    public List<CategoryExpense> getCategoryExpenses() {
        return transactionReadJpaRepository.findCategoryExpenses().stream()
                .map(result -> new CategoryExpense(result.getCategory(), zeroIfNull(result.getAmount()), BigDecimal.ZERO))
                .toList();
    }

    @Override
    public List<MerchantExpense> getTopMerchants() {
        return transactionReadJpaRepository.findTopMerchantExpenses(PageRequest.of(0, TOP_MERCHANTS_LIMIT)).stream()
                .map(result -> new MerchantExpense(
                        result.getMerchant(),
                        zeroIfNull(result.getAmount()),
                        result.getTransactionCount()
                ))
                .toList();
    }

    @Override
    public List<MonthlyAnalytics> getMonthlyAnalytics() {
        return transactionReadJpaRepository.findMonthlyAnalytics().stream()
                .map(result -> new MonthlyAnalytics(
                        YearMonth.of(result.getYear(), result.getMonth()),
                        zeroIfNull(result.getIncome()),
                        zeroIfNull(result.getExpense())
                ))
                .toList();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
