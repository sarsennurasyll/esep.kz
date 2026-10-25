package com.esep.statementimport.web;

import jakarta.servlet.http.HttpServletResponse;
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
    public String handleApiException(StatementApiException exception, Model model, HttpServletResponse response) {
        if (exception.getStatusCode() == 404) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("message", "Запрошенная выписка не найдена.");
            return "error/404";
        }

        logWithoutDetails(exception);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("message", "Не удалось получить данные выписки.");
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedException(Exception exception, Model model, HttpServletResponse response) {
        logWithoutDetails(exception);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("message", "Произошла непредвиденная ошибка. Попробуйте ещё раз позже.");
        return "error/500";
    }

    // Текст исключения и его причины может содержать данные выписки, поэтому в лог пишутся только классы.
    private void logWithoutDetails(Exception exception) {
        Throwable cause = exception.getCause();
        log.error("Ошибка web-интерфейса выписок: {}{}", exception.getClass().getName(),
                cause == null ? "" : " вызвана " + cause.getClass().getName());
    }
}
