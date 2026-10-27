package com.esep.statementimport.api;

import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.model.ImportErrorCode;
import com.esep.statementimport.pdf.PdfExtractionException;
import com.esep.statementimport.pdf.PdfLimitExceededException;
import com.esep.statementimport.pdf.PdfProcessingTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class StatementApiErrorsTest {

    @Test
    void shouldMapPdfProblemsToDistinctStatuses() {
        assertThat(StatementApiErrors.statusOf(new PdfLimitExceededException(ImportErrorCode.PDF_TOO_MANY_PAGES, "Слишком много страниц")))
                .isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(StatementApiErrors.statusOf(new PdfProcessingTimeoutException("Время истекло")))
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(StatementApiErrors.statusOf(new PdfExtractionException("Файл не является PDF-документом.", null)))
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(StatementApiErrors.statusOf(new StatementAlreadyImportedException("a".repeat(64))))
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldExposeOnlyOwnMessagesOfClientErrors() {
        assertThat(StatementApiErrors.clientMessageOf(new PdfLimitExceededException(ImportErrorCode.PDF_TOO_MANY_PAGES, "PDF-документ содержит больше 200 страниц.")))
                .isEqualTo("PDF-документ содержит больше 200 страниц.");
        assertThat(StatementApiErrors.clientMessageOf(new IllegalStateException("01.08.26 - 1 000,00 ₸ Перевод Тестов Т.")))
                .isEqualTo(StatementApiErrors.INTERNAL_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnCodeOnlyForTypedImportErrors() {
        assertThat(StatementApiErrors.codeOf(new PdfExtractionException(ImportErrorCode.PDF_ENCRYPTED, "PDF-документ защищён паролем.", null)))
                .isEqualTo(ImportErrorCode.PDF_ENCRYPTED);
        assertThat(StatementApiErrors.codeOf(new PdfProcessingTimeoutException("Время истекло"))).isEqualTo(ImportErrorCode.PDF_TIMEOUT);
        assertThat(StatementApiErrors.codeOf(new IllegalArgumentException("For input string: \"1 000,00\""))).isNull();
    }
}
