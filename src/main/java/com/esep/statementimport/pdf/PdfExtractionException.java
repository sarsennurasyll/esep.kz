package com.esep.statementimport.pdf;

/**
 * Ошибка извлечения текста из PDF-документа.
 */
public class PdfExtractionException extends RuntimeException {

    public PdfExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
