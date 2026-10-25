package com.esep.merchantmanagement.web;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Обработчик ошибок web-интерфейса базы знаний: несуществующий или некорректный идентификатор — страница 404.
 */
@ControllerAdvice(assignableTypes = KnowledgeBaseWebController.class)
public class KnowledgeBaseWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseWebExceptionHandler.class);

    @ExceptionHandler(MerchantApiException.class)
    public String handleApiException(MerchantApiException exception, Model model, HttpServletResponse response) {
        if (exception.getStatusCode() == 404 || exception.getStatusCode() == 400) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("message", "Запрошенный продавец не найден.");
            return "error/404";
        }
        return handleException(exception, model, response);
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, Model model, HttpServletResponse response) {
        // В лог пишется только класс исключения: его текст может содержать данные выписки.
        log.error("Ошибка web-интерфейса базы знаний: {}", exception.getClass().getName());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("message", "Не удалось загрузить базу знаний. Попробуйте ещё раз позже.");
        return "error/500";
    }
}
