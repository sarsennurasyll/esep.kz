package com.esep.merchantmanagement.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Обработчик ошибок web-интерфейса продавцов.
 */
@ControllerAdvice(assignableTypes = MerchantWebController.class)
public class MerchantWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MerchantWebExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception exception, Model model) {
        // В лог пишется только класс исключения: его текст может содержать данные выписки.
        log.error("Ошибка web-интерфейса продавцов: {}", exception.getClass().getName());
        model.addAttribute("message", "Не удалось загрузить данные продавцов. Попробуйте ещё раз позже.");
        return "error/500";
    }
}
