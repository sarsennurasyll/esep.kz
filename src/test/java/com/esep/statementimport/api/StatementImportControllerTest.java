package com.esep.statementimport.api;

import com.esep.security.LocalSecurityConfiguration;
import org.springframework.context.annotation.Import;
import com.esep.entity.BankType;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.exception.StatementFormatException;
import com.esep.statementimport.model.ImportErrorCode;
import com.esep.statementimport.exception.StatementWithoutTransactionsException;
import com.esep.statementimport.exception.UnsupportedBankTypeException;
import com.esep.statementimport.model.StatementImportResult;
import com.esep.statementimport.pdf.PdfExtractionException;
import com.esep.statementimport.pdf.PdfLimitExceededException;
import com.esep.statementimport.pdf.PdfProcessingTimeoutException;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatementImportController.class)
@Import(LocalSecurityConfiguration.class)
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
                        6,
                        3,
                        1,
                        2,
                        0,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31)
                ));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content"))
                        .param("bankType", "KASPI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementId").value(42))
                .andExpect(jsonPath("$.operationsTotal").value(6))
                .andExpect(jsonPath("$.recognizedOperations").value(3))
                .andExpect(jsonPath("$.unknownOperations").value(1))
                .andExpect(jsonPath("$.withoutMerchantOperations").value(2))
                .andExpect(jsonPath("$.periodFrom").value("2026-07-01"))
                .andExpect(jsonPath("$.periodTo").value("2026-07-31"));
    }

    @Test
    void shouldReturnConflictForRepeatedImport() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("statement.pdf")))
                .thenThrow(new StatementAlreadyImportedException("a".repeat(64)));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content"))
                        .param("bankType", "KASPI"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestForEmptyStatement() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("empty.pdf")))
                .thenThrow(new StatementWithoutTransactionsException());

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("empty.pdf", ""))
                        .param("bankType", "KASPI"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidPdf() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("invalid.pdf")))
                .thenThrow(new PdfExtractionException("Invalid PDF", new IOException("Invalid PDF")));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("invalid.pdf", "not a pdf"))
                        .param("bankType", "KASPI"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForBankWithoutRegisteredParser() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.HALYK), eq("statement.pdf")))
                .thenThrow(new UnsupportedBankTypeException(BankType.HALYK));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content"))
                        .param("bankType", "HALYK"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForStatementWithUnexpectedFormat() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("statement.pdf")))
                .thenThrow(new StatementFormatException("Сумма операций типа PURCHASE не совпадает с итогом в шапке выписки"));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content"))
                        .param("bankType", "KASPI"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPayloadTooLargeAndUnprocessableForPdfLimits() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("statement.pdf")))
                .thenThrow(new PdfLimitExceededException(ImportErrorCode.PDF_TOO_MANY_PAGES, "PDF-документ содержит больше 200 страниц."))
                .thenThrow(new PdfProcessingTimeoutException("Обработка PDF-документа заняла слишком много времени и была остановлена."));

        mockMvc.perform(multipart("/api/statements/import").file(pdfFile("statement.pdf", "content")).param("bankType", "KASPI"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.message").value("PDF-документ содержит больше 200 страниц."))
                .andExpect(jsonPath("$.code").value("PDF_TOO_MANY_PAGES"));
        mockMvc.perform(multipart("/api/statements/import").file(pdfFile("statement.pdf", "content")).param("bankType", "KASPI"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturnEncryptedCodeAndNoCodeForUntypedErrors() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("statement.pdf")))
                .thenThrow(new PdfExtractionException(ImportErrorCode.PDF_ENCRYPTED, "PDF-документ защищён паролем. Загрузите выписку без пароля.", null))
                .thenThrow(new StatementWithoutTransactionsException());

        mockMvc.perform(multipart("/api/statements/import").file(pdfFile("statement.pdf", "content")).param("bankType", "KASPI"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PDF_ENCRYPTED"));
        mockMvc.perform(multipart("/api/statements/import").file(pdfFile("statement.pdf", "content")).param("bankType", "KASPI"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldNotExposeUnexpectedErrorDetails() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("statement.pdf")))
                .thenThrow(new IllegalStateException("01.08.26 - 1 000,00 ₸ Перевод Тестов Т."));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content"))
                        .param("bankType", "KASPI"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Внутренняя ошибка сервера"));
    }

    @Test
    void shouldRejectCrossOriginImportFromBrowser() throws Exception {
        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content"))
                        .param("bankType", "KASPI")
                        .header("Origin", "https://attacker.example"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(statementImportUseCase);
    }

    @Test
    void shouldAcceptImportFromOwnOrigin() throws Exception {
        when(statementImportUseCase.importStatement(any(InputStream.class), eq(BankType.KASPI), eq("statement.pdf")))
                .thenReturn(new StatementImportResult(42L, 1, 1, 0, 0, 0, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)));

        mockMvc.perform(multipart("/api/statements/import")
                        .file(pdfFile("statement.pdf", "content"))
                        .param("bankType", "KASPI")
                        .header("Origin", "http://localhost"))
                .andExpect(status().isOk());
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
