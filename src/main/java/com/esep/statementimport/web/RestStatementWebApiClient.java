package com.esep.statementimport.web;

import com.esep.statementimport.api.dto.StatementImportResponse;
import com.esep.statementimport.api.dto.StatementResponse;
import com.esep.statementimport.api.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Реализация клиента, использующая существующий HTTP API приложения.
 */
@Component
public class RestStatementWebApiClient implements StatementWebApiClient {

    private final RestClient restClient;

    public RestStatementWebApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${server.port:8080}") String serverPort
    ) {
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:" + serverPort + "/api/statements")
                .build();
    }

    @Override
    public List<StatementResponse> findAllStatements() {
        return Arrays.asList(execute(() -> restClient.get()
                .retrieve()
                .body(StatementResponse[].class)));
    }

    @Override
    public StatementResponse findStatementById(Long statementId) {
        return execute(() -> restClient.get()
                .uri("/{statementId}", statementId)
                .retrieve()
                .body(StatementResponse.class));
    }

    @Override
    public List<TransactionResponse> findTransactionsByStatementId(Long statementId) {
        return Arrays.asList(execute(() -> restClient.get()
                .uri("/{statementId}/transactions", statementId)
                .retrieve()
                .body(TransactionResponse[].class)));
    }

    @Override
    public StatementImportResponse importStatement(MultipartFile file) {
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            return execute(() -> restClient.post()
                    .uri("/import")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(java.util.Map.of("file", resource))
                    .retrieve()
                    .body(StatementImportResponse.class));
        } catch (IOException exception) {
            throw new StatementApiException(500, "Не удалось прочитать выбранный файл.", exception);
        }
    }

    private <T> T execute(ApiCall<T> call) {
        try {
            return call.execute();
        } catch (RestClientResponseException exception) {
            throw new StatementApiException(
                    exception.getStatusCode().value(),
                    "Сервис выписок вернул ошибку.",
                    exception
            );
        }
    }

    @FunctionalInterface
    private interface ApiCall<T> {

        T execute();
    }
}
