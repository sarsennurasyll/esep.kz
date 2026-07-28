package com.esep.statementimport.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Обработчик ошибок web-интерфейса выписок.
 */
@ControllerAdvice(assignableTypes = StatementWebController.class)
public class StatementWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StatementWebExceptionHandler.class);

    @ExceptionHandler(StatementApiException.class)
    public String handleApiException(StatementApiException exception, Model model) {
        if (exception.getStatusCode() == 404) {
            model.addAttribute("message", "Запрошенная выписка не найдена.");
            return "error/404";
        }

        log.error("Не удалось обработать запрос web-интерфейса выписок.", exception);
        model.addAttribute("message", "Не удалось получить данные выписки.");
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedException(Exception exception, Model model) {
        log.error("Непредвиденная ошибка в web-интерфейсе выписок.", exception);
        model.addAttribute("message", "Произошла непредвиденная ошибка. Попробуйте ещё раз позже.");
        return "error/500";
    }
}
