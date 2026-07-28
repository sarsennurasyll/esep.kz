package com.esep.analytics.api;

import com.esep.analytics.api.dto.AnalyticsSummaryResponse;
import com.esep.analytics.api.dto.CategoryOperationCountResponse;
import com.esep.analytics.api.dto.CategoryExpenseResponse;
import com.esep.analytics.api.dto.MerchantExpenseResponse;
import com.esep.analytics.api.dto.MerchantTypeExpenseResponse;
import com.esep.analytics.api.dto.MonthlyAnalyticsResponse;
import com.esep.analytics.api.dto.PersonTransferResponse;
import com.esep.analytics.interfaces.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP API аналитики импортированных банковских операций.
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Аналитика импортированных операций")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Получить сводные показатели")
    public AnalyticsSummaryResponse getSummary() {
        return AnalyticsSummaryResponse.from(analyticsService.getSummary());
    }

    @GetMapping("/categories")
    @Operation(summary = "Получить расходы по категориям")
    public List<CategoryExpenseResponse> getCategoryExpenses() {
        return analyticsService.getCategoryExpenses().stream()
                .map(CategoryExpenseResponse::from)
                .toList();
    }

    @GetMapping("/category-counts")
    @Operation(summary = "Получить количество расходных операций по категориям")
    public List<CategoryOperationCountResponse> getCategoryOperationCounts() {
        return analyticsService.getCategoryOperationCounts().stream()
                .map(CategoryOperationCountResponse::from)
                .toList();
    }

    @GetMapping("/merchants")
    @Operation(summary = "Получить TOP-20 продавцов по расходам")
    public List<MerchantExpenseResponse> getTopMerchants() {
        return analyticsService.getTopMerchants().stream()
                .map(MerchantExpenseResponse::from)
                .toList();
    }

    @GetMapping("/types")
    @Operation(summary = "Получить расходы по типам получателей платежей")
    public List<MerchantTypeExpenseResponse> getMerchantTypeExpenses() {
        return analyticsService.getMerchantTypeExpenses().stream()
                .map(MerchantTypeExpenseResponse::from)
                .toList();
    }

    @GetMapping("/people")
    @Operation(summary = "Получить топ исходящих переводов людям")
    public List<PersonTransferResponse> getTopPersonTransfers() {
        return analyticsService.getTopPersonTransfers().stream()
                .map(PersonTransferResponse::from)
                .toList();
    }

    @GetMapping("/monthly")
    @Operation(summary = "Получить помесячные доходы и расходы")
    public List<MonthlyAnalyticsResponse> getMonthlyAnalytics() {
        return analyticsService.getMonthlyAnalytics().stream()
                .map(MonthlyAnalyticsResponse::from)
                .toList();
    }
}
