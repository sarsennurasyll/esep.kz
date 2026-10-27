package com.esep.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Стандартизированное описание ошибки HTTP API.
 *
 * @param code стабильный код причины, если он определён; клиенты выбирают по нему собственный текст
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(String message, String code) {

    public ApiErrorResponse(String message) {
        this(message, null);
    }
}
