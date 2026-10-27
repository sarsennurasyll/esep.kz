package com.esep.statementimport.pdf;

/**
 * Разбор PDF-документа не уложился в отведённое время и был остановлен.
 */
public class PdfProcessingTimeoutException extends PdfExtractionException {

    public PdfProcessingTimeoutException(String message) {
        super(message, null);
    }
}
