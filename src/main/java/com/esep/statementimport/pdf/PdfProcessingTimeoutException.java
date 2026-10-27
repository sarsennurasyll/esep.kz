package com.esep.statementimport.pdf;

import com.esep.statementimport.model.ImportErrorCode;

/**
 * Разбор PDF-документа не уложился в отведённое время и был остановлен.
 */
public class PdfProcessingTimeoutException extends PdfExtractionException {

    public PdfProcessingTimeoutException(String message) {
        super(ImportErrorCode.PDF_TIMEOUT, message, null);
    }
}
