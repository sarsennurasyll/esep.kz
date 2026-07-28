package com.esep.merchantmanagement.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Обработчик ошибок web-интерфейса продавцов.
 */
@ControllerAdvice(assignableTypes = MerchantWebController.class)
public class MerchantWebExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Model model) {
        model.addAttribute("message", "Не удалось загрузить данные продавцов. Попробуйте ещё раз позже.");
        return "error/500";
    }
}
