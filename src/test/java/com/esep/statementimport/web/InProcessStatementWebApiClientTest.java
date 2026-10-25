package com.esep.statementimport.web;

import com.esep.entity.BankType;
import com.esep.statementimport.api.StatementImportController;
import com.esep.statementimport.api.StatementQueryController;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.exception.StatementFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InProcessStatementWebApiClientTest {

    private final StatementImportController importController = mock(StatementImportController.class);
    private final StatementQueryController queryController = mock(StatementQueryController.class);
    private final InProcessStatementWebApiClient client = new InProcessStatementWebApiClient(importController, queryController);
    private final MockMultipartFile file = new MockMultipartFile("file", "statement.pdf", "application/pdf", new byte[]{1});

    @Test
    void shouldReturnNotFoundForMissingStatement() {
        when(queryController.findById(7L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> client.findStatementById(7L))
                .isInstanceOfSatisfying(StatementApiException.class, exception -> assertThat(exception.getStatusCode()).isEqualTo(404));
    }

    @Test
    void shouldMapImportErrorsToSameStatusesAsHttpApi() throws Exception {
        when(importController.importStatement(any(), any(BankType.class)))
                .thenThrow(new StatementAlreadyImportedException("a".repeat(64)))
                .thenThrow(new StatementFormatException("Сумма операций типа PURCHASE не совпадает с итогом в шапке выписки"))
                .thenThrow(new IllegalStateException("01.08.26 - 1 000,00 ₸ Перевод Тестов Т."));

        assertThat(statusOfImport()).isEqualTo(409);
        assertThat(statusOfImport()).isEqualTo(400);
        assertThatThrownBy(() -> client.importStatement(file, BankType.KASPI))
                .isInstanceOfSatisfying(StatementApiException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(500);
                    assertThat(exception.getMessage()).doesNotContain("Тестов");
                });
    }

    private int statusOfImport() {
        try {
            client.importStatement(file, BankType.KASPI);
            return 200;
        } catch (StatementApiException exception) {
            return exception.getStatusCode();
        }
    }
}
