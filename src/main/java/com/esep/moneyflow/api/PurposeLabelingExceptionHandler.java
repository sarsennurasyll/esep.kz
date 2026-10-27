package com.esep.moneyflow.api;

import com.esep.common.api.ApiErrorResponse;
import com.esep.moneyflow.exception.OperationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Преобразует ошибки разметки назначения в ответы HTTP API.
 */
@RestControllerAdvice(assignableTypes = PurposeLabelingController.class)
public class PurposeLabelingExceptionHandler {

    @ExceptionHandler(OperationNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleOperationNotFound(OperationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(exception.getMessage()));
    }
}
