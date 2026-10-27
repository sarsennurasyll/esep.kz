package com.esep.statementimport.pdf;

import com.esep.statementimport.model.ImportErrorCode;

/**
 * PDF-документ превышает допустимый объём обработки: страницы, извлечённый текст или память разбора.
 */
public class PdfLimitExceededException extends PdfExtractionException {

    public PdfLimitExceededException(ImportErrorCode code, String message) {
        super(code, message, null);
    }
}
