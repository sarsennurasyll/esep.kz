package com.esep.statementimport.api;

import com.esep.parser.common.ParserException;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.exception.StatementFormatException;
import com.esep.statementimport.exception.StatementWithoutTransactionsException;
import com.esep.statementimport.pdf.PdfExtractionException;
import org.springframework.http.HttpStatus;

/**
 * Единое соответствие ошибок импорта и просмотра выписок HTTP-статусам.
 * Используется REST-обработчиком и web-клиентом, вызывающим API в том же процессе.
 */
public final class StatementApiErrors {

    public static final String INTERNAL_ERROR_MESSAGE = "Внутренняя ошибка сервера";

    private StatementApiErrors() {
    }

    public static HttpStatus statusOf(Throwable exception) {
        if (exception instanceof StatementAlreadyImportedException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof StatementWithoutTransactionsException
                || exception instanceof StatementFormatException
                || exception instanceof PdfExtractionException
                || exception instanceof ParserException
                || exception instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    // Текст непредвиденного исключения может содержать данные выписки, поэтому наружу отдаётся общее сообщение.
    public static String clientMessageOf(Throwable exception) {
        return statusOf(exception).is5xxServerError() ? INTERNAL_ERROR_MESSAGE : exception.getMessage();
    }
}
