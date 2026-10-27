package com.esep.statementimport.web;

import com.esep.statementimport.model.ImportErrorCode;

/**
 * Ошибка ответа HTTP API выписок.
 */
public class StatementApiException extends RuntimeException {

    private final int statusCode;
    private final ImportErrorCode errorCode;

    public StatementApiException(int statusCode, String message, Throwable cause) {
        this(statusCode, null, message, cause);
    }

    public StatementApiException(int statusCode, ImportErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    /** Код причины отказа или {@code null}, если причина не типизирована. */
    public ImportErrorCode getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
