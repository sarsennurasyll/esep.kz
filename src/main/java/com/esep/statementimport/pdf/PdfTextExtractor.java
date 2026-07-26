package com.esep.statementimport.pdf;

import java.io.InputStream;

/**
 * Контракт извлечения текста из PDF-документа.
 */
public interface PdfTextExtractor {

    String extract(InputStream input);
}
