package com.esep.statementimport.web;

/**
 * Ошибка ответа HTTP API выписок.
 */
public class StatementApiException extends RuntimeException {

    private final int statusCode;

    public StatementApiException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
