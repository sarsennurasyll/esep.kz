package com.esep.statementimport.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Обработчик ошибок web-интерфейса выписок.
 */
@ControllerAdvice(assignableTypes = StatementWebController.class)
public class StatementWebExceptionHandler {

    @ExceptionHandler(StatementApiException.class)
    public String handleApiException(StatementApiException exception, Model model) {
        if (exception.getStatusCode() == 404) {
            model.addAttribute("message", "Запрошенная выписка не найдена.");
            return "error/404";
        }

        model.addAttribute("message", "Не удалось получить данные выписки.");
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedException(Model model) {
        model.addAttribute("message", "Произошла непредвиденная ошибка. Попробуйте ещё раз позже.");
        return "error/500";
    }
}
