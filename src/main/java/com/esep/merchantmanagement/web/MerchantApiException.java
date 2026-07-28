package com.esep.merchantmanagement.web;

/**
 * Ошибка ответа HTTP API продавцов.
 */
public class MerchantApiException extends RuntimeException {

    private final int statusCode;

    public MerchantApiException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
