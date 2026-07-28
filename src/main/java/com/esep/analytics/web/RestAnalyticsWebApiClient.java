package com.esep.analytics.web;

import com.esep.analytics.api.dto.AnalyticsSummaryResponse;
import com.esep.analytics.api.dto.CategoryOperationCountResponse;
import com.esep.analytics.api.dto.CategoryExpenseResponse;
import com.esep.analytics.api.dto.MerchantExpenseResponse;
import com.esep.analytics.api.dto.MerchantTypeExpenseResponse;
import com.esep.analytics.api.dto.MonthlyAnalyticsResponse;
import com.esep.analytics.api.dto.PersonTransferResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

/**
 * Реализация клиента, использующая существующий HTTP API аналитики.
 */
@Component
public class RestAnalyticsWebApiClient implements AnalyticsWebApiClient {

    private final RestClient restClient;

    public RestAnalyticsWebApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${server.port:8080}") String serverPort
    ) {
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:" + serverPort + "/api/analytics")
                .build();
    }

    @Override
    public AnalyticsSummaryResponse getSummary() {
        return restClient.get().uri("/summary").retrieve().body(AnalyticsSummaryResponse.class);
    }

    @Override
    public List<CategoryExpenseResponse> getCategoryExpenses() {
        return Arrays.asList(restClient.get().uri("/categories").retrieve().body(CategoryExpenseResponse[].class));
    }

    @Override
    public List<CategoryOperationCountResponse> getCategoryOperationCounts() {
        return Arrays.asList(restClient.get()
                .uri("/category-counts")
                .retrieve()
                .body(CategoryOperationCountResponse[].class));
    }

    @Override
    public List<MerchantExpenseResponse> getTopMerchants() {
        return Arrays.asList(restClient.get().uri("/merchants").retrieve().body(MerchantExpenseResponse[].class));
    }

    @Override
    public List<MerchantTypeExpenseResponse> getMerchantTypeExpenses() {
        return Arrays.asList(restClient.get()
                .uri("/types")
                .retrieve()
                .body(MerchantTypeExpenseResponse[].class));
    }

    @Override
    public List<PersonTransferResponse> getTopPersonTransfers() {
        return Arrays.asList(restClient.get()
                .uri("/people")
                .retrieve()
                .body(PersonTransferResponse[].class));
    }

    @Override
    public List<MonthlyAnalyticsResponse> getMonthlyAnalytics() {
        return Arrays.asList(restClient.get().uri("/monthly").retrieve().body(MonthlyAnalyticsResponse[].class));
    }
}
