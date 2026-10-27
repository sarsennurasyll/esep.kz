package com.esep.statementimport.pdf;

/**
 * PDF-документ превышает допустимый объём обработки: страницы, извлечённый текст или память разбора.
 */
public class PdfLimitExceededException extends PdfExtractionException {

    public PdfLimitExceededException(String message) {
        super(message, null);
    }
}
