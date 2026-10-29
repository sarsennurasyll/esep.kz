package com.esep.analytics.web;

import com.esep.security.LocalSecurityConfiguration;
import org.springframework.context.annotation.Import;
import com.esep.analytics.api.dto.AnalyticsSummaryResponse;
import com.esep.analytics.api.dto.MoneyFlowReportResponse;
import com.esep.analytics.api.dto.MonthlyMoneyFlowResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DashboardWebController.class)
@Import(LocalSecurityConfiguration.class)
class DashboardWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsWebApiClient analyticsWebApiClient;

    @Test
    void shouldRenderDashboard() throws Exception {
        when(analyticsWebApiClient.getSummary()).thenReturn(new AnalyticsSummaryResponse(
                0, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0
        ));
        when(analyticsWebApiClient.getCategoryExpenses()).thenReturn(List.of());
        when(analyticsWebApiClient.getTopMerchants()).thenReturn(List.of());
        MoneyFlowReportResponse report = new MoneyFlowReportResponse(
                BigDecimal.ZERO, new BigDecimal("39000"), new BigDecimal("20000"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("30000"), BigDecimal.ZERO, new BigDecimal("70000"), new BigDecimal("35000"),
                new BigDecimal("101500"), new BigDecimal("95500"), new BigDecimal("0.6897"), new BigDecimal("0.5759")
        );
        when(analyticsWebApiClient.getMoneyFlowReport()).thenReturn(report);
        when(analyticsWebApiClient.getMonthlyMoneyFlowReports()).thenReturn(List.of(new MonthlyMoneyFlowResponse("2026-08", report, false)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("summary", "moneyFlow", "categories", "merchants", "monthlyMoneyFlow"))
                .andExpect(content().string(containsString("2026-08")))
                .andExpect(content().string(containsString("69")))
                .andExpect(content().string(containsString("неполный")))
                .andExpect(content().string(containsString("\"month\":\"2026-08\",\"report\":{")))
                .andExpect(content().string(containsString("Движение денег")))
                .andExpect(content().string(containsString("Назначение неизвестно")));
    }
}
