package com.esep.analytics.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Обработчик ошибок web-страницы dashboard.
 */
@ControllerAdvice(assignableTypes = DashboardWebController.class)
public class DashboardWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DashboardWebExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception exception, Model model) {
        // В лог пишется только класс исключения: его текст может содержать данные выписки.
        log.error("Ошибка web-интерфейса dashboard: {}", exception.getClass().getName());
        model.addAttribute("message", "Не удалось загрузить аналитические данные. Попробуйте ещё раз позже.");
        return "error/500";
    }
}
