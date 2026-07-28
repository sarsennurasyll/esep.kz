package com.esep.analytics.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC-контроллер главной страницы аналитического dashboard.
 */
@Controller
public class DashboardWebController {

    private final AnalyticsWebApiClient analyticsWebApiClient;

    public DashboardWebController(AnalyticsWebApiClient analyticsWebApiClient) {
        this.analyticsWebApiClient = analyticsWebApiClient;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("summary", analyticsWebApiClient.getSummary());
        model.addAttribute("categories", analyticsWebApiClient.getCategoryExpenses());
        model.addAttribute("merchants", analyticsWebApiClient.getTopMerchants());
        model.addAttribute("monthly", analyticsWebApiClient.getMonthlyAnalytics());
        return "dashboard";
    }
}
