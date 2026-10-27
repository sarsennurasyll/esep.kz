package com.esep.statementimport.model;

/**
 * Стабильные коды причин отказа в импорте выписки. По коду клиент выбирает собственный текст сообщения,
 * поэтому наружу не передаются ни сырые тексты исключений, ни содержимое файла.
 */
public enum ImportErrorCode {
    PDF_NOT_PDF,
    PDF_ENCRYPTED,
    PDF_CORRUPTED,
    PDF_TOO_MANY_PAGES,
    PDF_TOO_MUCH_TEXT,
    PDF_TOO_COMPLEX,
    PDF_TIMEOUT,
    UPLOAD_TOO_LARGE
}
