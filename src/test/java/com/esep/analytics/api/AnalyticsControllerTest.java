package com.esep.analytics.api;

import com.esep.analytics.interfaces.AnalyticsService;
import com.esep.analytics.model.AnalyticsSummary;
import com.esep.analytics.model.CategoryOperationCount;
import com.esep.analytics.model.CategoryExpense;
import com.esep.analytics.model.MerchantExpense;
import com.esep.analytics.model.MerchantTypeExpense;
import com.esep.analytics.model.MonthlyAnalytics;
import com.esep.analytics.model.PersonTransfer;
import com.esep.entity.MerchantType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    void shouldReturnSummary() throws Exception {
        when(analyticsService.getSummary()).thenReturn(new AnalyticsSummary(
                2, 5, new BigDecimal("1000.00"), new BigDecimal("450.00"), 4, 1
        ));

        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStatements").value(2))
                .andExpect(jsonPath("$.totalTransactions").value(5))
                .andExpect(jsonPath("$.totalIncome").value(1000.00))
                .andExpect(jsonPath("$.totalExpense").value(450.00))
                .andExpect(jsonPath("$.recognizedTransactions").value(4))
                .andExpect(jsonPath("$.unknownTransactions").value(1));
    }

    @Test
    void shouldReturnCategoryMerchantAndMonthlyAnalytics() throws Exception {
        when(analyticsService.getCategoryExpenses()).thenReturn(List.of(
                new CategoryExpense("GROCERY", "Продукты", new BigDecimal("300.00"), new BigDecimal("75.00"))
        ));
        when(analyticsService.getTopMerchants()).thenReturn(List.of(
                new MerchantExpense("MAGNUM", new BigDecimal("300.00"), 2)
        ));
        when(analyticsService.getMonthlyAnalytics()).thenReturn(List.of(
                new MonthlyAnalytics(YearMonth.of(2026, 7), new BigDecimal("1000.00"), new BigDecimal("300.00"))
        ));

        mockMvc.perform(get("/api/analytics/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("GROCERY"))
                .andExpect(jsonPath("$[0].categoryName").value("Продукты"));
        mockMvc.perform(get("/api/analytics/merchants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].merchant").value("MAGNUM"));
        mockMvc.perform(get("/api/analytics/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2026-07"));
    }

    @Test
    void shouldReturnTypePersonAndCategoryCountAnalytics() throws Exception {
        when(analyticsService.getMerchantTypeExpenses()).thenReturn(List.of(
                new MerchantTypeExpense(MerchantType.PERSON, new BigDecimal("500.00"), 2)
        ));
        when(analyticsService.getTopPersonTransfers()).thenReturn(List.of(
                new PersonTransfer("ЕРАСЫЛ Е", new BigDecimal("500.00"), 2)
        ));
        when(analyticsService.getCategoryOperationCounts()).thenReturn(List.of(
                new CategoryOperationCount("PERSONAL_TRANSFERS", "Переводы людям", 2)
        ));

        mockMvc.perform(get("/api/analytics/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].merchantType").value("PERSON"))
                .andExpect(jsonPath("$[0].merchantTypeName").value("Перевод человеку"));
        mockMvc.perform(get("/api/analytics/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipient").value("ЕРАСЫЛ Е"));
        mockMvc.perform(get("/api/analytics/category-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Переводы людям"))
                .andExpect(jsonPath("$[0].transactionCount").value(2));
    }
}
