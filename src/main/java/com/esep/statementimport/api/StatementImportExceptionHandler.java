package com.esep.statementimport.api;

import com.esep.common.api.ApiErrorResponse;
import com.esep.statementimport.model.ImportErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Преобразует ошибки импорта в HTTP-ответы API.
 */
@RestControllerAdvice(assignableTypes = {
        StatementImportController.class,
        StatementQueryController.class
})
public class StatementImportExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StatementImportExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handle(Exception exception) {
        HttpStatus status = StatementApiErrors.statusOf(exception);
        if (status.is5xxServerError()) {
            log.error("Непредвиденная ошибка при обработке запроса к API выписок: {}", exception.getClass().getName());
        }
        ImportErrorCode code = StatementApiErrors.codeOf(exception);
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                StatementApiErrors.clientMessageOf(exception),
                code == null ? null : code.name()
        ));
    }
}
