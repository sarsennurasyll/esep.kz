package com.esep.common.web;

import com.esep.common.api.ApiErrorResponse;
import com.esep.statementimport.model.ImportErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Отвечает на файл сверх лимита загрузки в исходном запросе. Стандартная передача на /error снова
 * разбирает то же multipart-тело, снова превышает лимит и возвращает пустой ответ.
 */
@ControllerAdvice
public class UploadLimitExceptionHandler {

    static final String MESSAGE = "Файл превышает допустимый размер выписки.";

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handle(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ApiErrorResponse(MESSAGE, ImportErrorCode.UPLOAD_TOO_LARGE.name()));
        }
        response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        return new ModelAndView("error/413");
    }
}
