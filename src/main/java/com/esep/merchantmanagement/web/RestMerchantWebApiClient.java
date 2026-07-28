package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.api.dto.MerchantMatchRequest;
import com.esep.merchantmanagement.api.dto.MerchantResponse;
import com.esep.merchantmanagement.api.dto.UnknownMerchantResponse;
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
    public List<UnknownMerchantResponse> findUnknownDescriptions() {
        return Arrays.asList(execute(() -> restClient.get()
                .uri("/unknown")
                .retrieve()
                .body(UnknownMerchantResponse[].class)));
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
