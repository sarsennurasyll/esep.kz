package com.esep.analytics.service;

import com.esep.analytics.interfaces.AnalyticsQuery;
import com.esep.analytics.model.AnalyticsSummary;
import com.esep.analytics.model.CategoryExpense;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultAnalyticsServiceTest {

    @Test
    void shouldCalculatePercentagesFromAggregatedCategoryAmounts() {
        AnalyticsQuery query = mock(AnalyticsQuery.class);
        when(query.getCategoryExpenses()).thenReturn(List.of(
                new CategoryExpense("GROCERY", "Продукты", new BigDecimal("300.00"), BigDecimal.ZERO),
                new CategoryExpense("TRANSPORT", "Транспорт", new BigDecimal("100.00"), BigDecimal.ZERO)
        ));

        var result = new DefaultAnalyticsService(query).getCategoryExpenses();

        assertThat(result).containsExactly(
                new CategoryExpense("GROCERY", "Продукты", new BigDecimal("300.00"), new BigDecimal("75.00")),
                new CategoryExpense("TRANSPORT", "Транспорт", new BigDecimal("100.00"), new BigDecimal("25.00"))
        );
    }

    @Test
    void shouldReturnZeroPercentForEmptyExpenseTotal() {
        AnalyticsQuery query = mock(AnalyticsQuery.class);
        when(query.getCategoryExpenses()).thenReturn(List.of(
                new CategoryExpense("UNCATEGORIZED", "Без категории", BigDecimal.ZERO, BigDecimal.ONE)
        ));

        var result = new DefaultAnalyticsService(query).getCategoryExpenses();

        assertThat(result).containsExactly(
                new CategoryExpense("UNCATEGORIZED", "Без категории", BigDecimal.ZERO, BigDecimal.ZERO)
        );
    }
}
