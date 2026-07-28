package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.api.dto.MerchantMatchRequest;
import com.esep.merchantmanagement.api.dto.MerchantResponse;
import com.esep.merchantmanagement.api.dto.UnknownMerchantResponse;
import com.esep.merchantmanagement.api.dto.MerchantLearningStatisticsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.List;

/**
 * Реализация клиента, использующая существующий HTTP API продавцов.
 */
@Component
public class RestMerchantWebApiClient implements MerchantWebApiClient {

    private final RestClient restClient;

    public RestMerchantWebApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${server.port:8080}") String serverPort
    ) {
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:" + serverPort + "/api/merchants")
                .build();
    }

    @Override
    public List<UnknownMerchantResponse> findUnknownDescriptions(String query, boolean onlyNew, Long minUsageCount, java.math.BigDecimal minTotalAmount) {
        return Arrays.asList(execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/unknown")
                        .queryParamIfPresent("query", optional(query))
                        .queryParamIfPresent("minUsageCount", optional(minUsageCount))
                        .queryParamIfPresent("minTotalAmount", optional(minTotalAmount))
                        .queryParam("onlyNew", onlyNew)
                        .build())
                .retrieve()
                .body(UnknownMerchantResponse[].class)));
    }

    private <T> java.util.Optional<T> optional(T value) {
        return value == null || value instanceof String string && string.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
    }

    @Override
    public List<MerchantResponse> findMerchants() {
        return Arrays.asList(execute(() -> restClient.get()
                .retrieve()
                .body(MerchantResponse[].class)));
    }

    @Override
    public void match(MerchantMatchRequest request) {
        execute(() -> restClient.post()
                .uri("/match")
                .body(request)
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public MerchantLearningStatisticsResponse learningStatistics() {
        return execute(() -> restClient.get().uri("/learning-statistics").retrieve().body(MerchantLearningStatisticsResponse.class));
    }

    private <T> T execute(ApiCall<T> call) {
        try {
            return call.execute();
        } catch (RestClientResponseException exception) {
            throw new MerchantApiException(
                    exception.getStatusCode().value(),
                    "Сервис продавцов вернул ошибку.",
                    exception
            );
        }
    }

    @FunctionalInterface
    private interface ApiCall<T> {

        T execute();
    }
}
