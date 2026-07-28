package com.esep.analytics.web;

import com.esep.analytics.api.dto.AnalyticsSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DashboardWebController.class)
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
        when(analyticsWebApiClient.getMonthlyAnalytics()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("summary", "categories", "merchants", "monthly"));
    }
}
