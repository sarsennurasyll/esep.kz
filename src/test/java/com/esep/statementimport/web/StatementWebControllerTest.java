package com.esep.statementimport.web;

import com.esep.entity.BankType;
import com.esep.entity.TransactionType;
import com.esep.statementimport.api.dto.StatementImportResponse;
import com.esep.statementimport.api.dto.StatementResponse;
import com.esep.statementimport.api.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(StatementWebController.class)
class StatementWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatementWebApiClient statementWebApiClient;

    @Test
    void shouldRenderHomeWithRecentStatements() throws Exception {
        when(statementWebApiClient.findAllStatements()).thenReturn(List.of(statement()));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("statements"));
    }

    @Test
    void shouldRedirectToImportedStatement() throws Exception {
        when(statementWebApiClient.importStatement(any())).thenReturn(new StatementImportResponse(
                42L,
                2,
                2,
                0,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        ));

        mockMvc.perform(multipart("/import")
                        .file(new MockMultipartFile("file", "statement.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes())))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/statements/42"));
    }

    @Test
    void shouldShowImportErrorAfterInvalidPdf() throws Exception {
        when(statementWebApiClient.importStatement(any()))
                .thenThrow(new StatementApiException(400, "Invalid PDF", null));

        mockMvc.perform(multipart("/import")
                        .file(new MockMultipartFile("file", "invalid.pdf", MediaType.APPLICATION_PDF_VALUE, "invalid".getBytes())))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/import"))
                .andExpect(flash().attributeExists("importError"));
    }

    @Test
    void shouldRenderStatementWithTransactions() throws Exception {
        when(statementWebApiClient.findStatementById(42L)).thenReturn(statement());
        when(statementWebApiClient.findTransactionsByStatementId(42L)).thenReturn(List.of(new TransactionResponse(
                LocalDate.of(2026, 7, 12),
                "MAGNUM CASH&CARRY",
                "MAGNUM",
                "GROCERY",
                new BigDecimal("-14500.00"),
                "KZT",
                TransactionType.EXPENSE
        )));

        mockMvc.perform(get("/statements/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("statement-details"))
                .andExpect(model().attributeExists("statement"))
                .andExpect(model().attributeExists("transactions"));
    }

    @Test
    void shouldRenderNotFoundPageForUnknownStatement() throws Exception {
        when(statementWebApiClient.findStatementById(99L))
                .thenThrow(new StatementApiException(404, "Not found", null));

        mockMvc.perform(get("/statements/99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "Запрошенная выписка не найдена."));
    }

    private StatementResponse statement() {
        return new StatementResponse(
                42L,
                BankType.KASPI,
                "statement.pdf",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                2,
                Instant.parse("2026-07-31T10:15:30Z")
        );
    }
}
