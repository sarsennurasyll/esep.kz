package com.esep.merchantmanagement.web;

import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.api.dto.CategoryOptionResponse;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseAliasRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class RestKnowledgeBaseWebApiClient implements KnowledgeBaseWebApiClient {

    private final RestClient restClient;

    public RestKnowledgeBaseWebApiClient(RestClient.Builder builder, @Value("${server.port:8080}") String serverPort) {
        this.restClient = builder.baseUrl("http://localhost:" + serverPort + "/api/knowledge-base").build();
    }

    @Override
    public List<KnowledgeBaseMerchantResponse> findMerchants(String query, MerchantType merchantType, String categoryCode) {
        return execute(() -> restClient.get().uri(uriBuilder -> uriBuilder.path("/merchants")
                        .queryParamIfPresent("query", optional(query))
                        .queryParamIfPresent("merchantType", optional(merchantType == null ? null : merchantType.name()))
                        .queryParamIfPresent("categoryCode", optional(categoryCode))
                        .build())
                .retrieve().body(new ParameterizedTypeReference<List<KnowledgeBaseMerchantResponse>>() {}));
    }

    @Override
    public KnowledgeBaseMerchantResponse findMerchant(String id) {
        return execute(() -> restClient.get().uri("/merchants/{id}", id).retrieve().body(KnowledgeBaseMerchantResponse.class));
    }

    @Override
    public List<CategoryOptionResponse> findCategories() {
        return execute(() -> restClient.get().uri("/categories").retrieve()
                .body(new ParameterizedTypeReference<List<CategoryOptionResponse>>() {}));
    }

    @Override
    public KnowledgeBaseMerchantResponse createMerchant(KnowledgeBaseMerchantRequest request) {
        return execute(() -> restClient.post().uri("/merchants").body(request).retrieve().body(KnowledgeBaseMerchantResponse.class));
    }

    @Override
    public void updateMerchant(String id, KnowledgeBaseMerchantRequest request) {
        execute(() -> restClient.put().uri("/merchants/{id}", id).body(request).retrieve().toBodilessEntity());
    }

    @Override
    public void deleteMerchant(String id) {
        execute(() -> restClient.delete().uri("/merchants/{id}", id).retrieve().toBodilessEntity());
    }

    @Override
    public void addAlias(String merchantId, KnowledgeBaseAliasRequest request) {
        execute(() -> restClient.post().uri("/merchants/{id}/aliases", merchantId).body(request).retrieve().toBodilessEntity());
    }

    @Override
    public void updateAlias(String merchantId, Long aliasId, KnowledgeBaseAliasRequest request) {
        execute(() -> restClient.put().uri("/merchants/{id}/aliases/{aliasId}", merchantId, aliasId).body(request).retrieve().toBodilessEntity());
    }

    @Override
    public void deleteAlias(String merchantId, Long aliasId) {
        execute(() -> restClient.delete().uri("/merchants/{id}/aliases/{aliasId}", merchantId, aliasId).retrieve().toBodilessEntity());
    }

    private java.util.Optional<String> optional(String value) {
        return value == null || value.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
    }

    private <T> T execute(ApiCall<T> call) {
        try {
            return call.execute();
        } catch (RestClientResponseException exception) {
            throw new MerchantApiException(exception.getStatusCode().value(), "Knowledge Base API returned an error.", exception);
        }
    }

    @FunctionalInterface
    private interface ApiCall<T> { T execute(); }
}
