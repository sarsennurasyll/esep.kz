package com.esep.merchantmanagement.api;

import com.esep.common.api.ApiErrorResponse;
import com.esep.merchantmanagement.exception.MerchantAliasAlreadyExistsException;
import com.esep.merchantmanagement.exception.MerchantNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Преобразует ошибки управления продавцами в ответы HTTP API.
 */
@RestControllerAdvice(assignableTypes = MerchantManagementController.class)
public class MerchantManagementExceptionHandler {

    @ExceptionHandler(MerchantNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleMerchantNotFound(MerchantNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(MerchantAliasAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> handleAliasAlreadyExists(MerchantAliasAlreadyExistsException exception) {
        return error(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, Exception exception) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(exception.getMessage()));
    }
}
