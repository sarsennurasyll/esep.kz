package com.esep.analytics.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Обработчик ошибок web-страницы dashboard.
 */
@ControllerAdvice(assignableTypes = DashboardWebController.class)
public class DashboardWebExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Model model) {
        model.addAttribute("message", "Не удалось загрузить аналитические данные. Попробуйте ещё раз позже.");
        return "error/500";
    }
}
