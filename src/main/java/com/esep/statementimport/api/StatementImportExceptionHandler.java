package com.esep.statementimport.api;

import com.esep.common.api.ApiErrorResponse;
import com.esep.parser.common.ParserException;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.exception.StatementWithoutTransactionsException;
import com.esep.statementimport.pdf.PdfExtractionException;
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

    @ExceptionHandler(StatementAlreadyImportedException.class)
    ResponseEntity<ApiErrorResponse> handleAlreadyImported(StatementAlreadyImportedException exception) {
        return error(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(StatementWithoutTransactionsException.class)
    ResponseEntity<ApiErrorResponse> handleWithoutTransactions(StatementWithoutTransactionsException exception) {
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler({PdfExtractionException.class, ParserException.class, IllegalArgumentException.class})
    ResponseEntity<ApiErrorResponse> handleParserError(RuntimeException exception) {
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, Exception exception) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(exception.getMessage()));
    }
}
