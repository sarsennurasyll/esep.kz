package com.esep.statementimport.pdf;

import com.esep.statementimport.model.ImportErrorCode;

import java.util.Objects;

/**
 * Ошибка извлечения текста из PDF-документа с типизированной причиной.
 */
public class PdfExtractionException extends RuntimeException {

    private final ImportErrorCode code;

    public PdfExtractionException(ImportErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "Error code must not be null");
    }

    public PdfExtractionException(String message, Throwable cause) {
        this(ImportErrorCode.PDF_CORRUPTED, message, cause);
    }

    public ImportErrorCode code() {
        return code;
    }
}
