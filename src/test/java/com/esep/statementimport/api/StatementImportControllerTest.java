package com.esep.statementimport.api;

import com.esep.entity.BankType;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.exception.StatementWithoutTransactionsException;
import com.esep.statementimport.model.StatementImportResult;
import com.esep.statementimport.pdf.PdfExtractionException;
import com.esep.statementimport.service.DefaultStatementImportUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatementImportController.class)
class StatementImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DefaultStatementImportUseCase statementImportUseCase;

    @Test
    void shouldImportKaspiStatement() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("statement.pdf")))
                .thenReturn(new StatementImportResult(
                        42L,
                        4,
                        3,
                        1,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31)
                ));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementId").value(42))
                .andExpect(jsonPath("$.operationsTotal").value(4))
                .andExpect(jsonPath("$.recognizedOperations").value(3))
                .andExpect(jsonPath("$.unknownOperations").value(1))
                .andExpect(jsonPath("$.periodFrom").value("2026-07-01"))
                .andExpect(jsonPath("$.periodTo").value("2026-07-31"));
    }

    @Test
    void shouldReturnConflictForRepeatedImport() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("statement.pdf")))
                .thenThrow(new StatementAlreadyImportedException("a".repeat(64)));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestForEmptyStatement() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("empty.pdf")))
                .thenThrow(new StatementWithoutTransactionsException());

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("empty.pdf", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidPdf() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("invalid.pdf")))
                .thenThrow(new PdfExtractionException("Invalid PDF", new IOException("Invalid PDF")));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("invalid.pdf", "not a pdf")))
                .andExpect(status().isBadRequest());
    }

    private MockMultipartFile pdfFile(String fileName, String content) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_PDF_VALUE,
                content.getBytes()
        );
    }
}
