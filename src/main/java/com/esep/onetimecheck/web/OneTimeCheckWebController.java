package com.esep.onetimecheck.web;

import com.esep.entity.BankType;
import com.esep.onetimecheck.service.OneTimeCheckService;
import com.esep.statementimport.api.StatementApiErrors;
import com.esep.statementimport.web.StatementErrorMessages;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Страницы разовой проверки выписки без регистрации (ADR-0007).
 * Отчёт возвращается прямо в ответе на загрузку: без перенаправления и без записи в сессию.
 */
@Controller
public class OneTimeCheckWebController {

    private static final Logger log = LoggerFactory.getLogger(OneTimeCheckWebController.class);
    static final String CONSENT_REQUIRED = "Чтобы проверить выписку, подтвердите согласие на её обработку.";

    private final OneTimeCheckService oneTimeCheckService;

    public OneTimeCheckWebController(OneTimeCheckService oneTimeCheckService) {
        this.oneTimeCheckService = oneTimeCheckService;
    }

    @GetMapping("/check")
    public String checkForm() {
        return "check";
    }

    @PostMapping("/check")
    public String check(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "bankType", defaultValue = "KASPI") BankType bankType,
            @RequestParam(name = "consent", defaultValue = "false") boolean consent,
            Model model,
            HttpServletResponse response
    ) {
        if (!consent) {
            return checkError(model, response, HttpStatus.BAD_REQUEST.value(), CONSENT_REQUIRED);
        }
        try (InputStream input = file.getInputStream()) {
            model.addAttribute("report", oneTimeCheckService.check(input, bankType));
            return "check-result";
        } catch (IOException | RuntimeException exception) {
            int status = StatementApiErrors.statusOf(exception).value();
            if (status >= 500) {
                // Текст исключения может содержать данные выписки, поэтому в лог пишется только класс.
                log.error("Разовая проверка выписки завершилась ошибкой: {}", exception.getClass().getName());
            }
            return checkError(model, response, status, StatementErrorMessages.of(status, StatementApiErrors.codeOf(exception)));
        }
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    private static String checkError(Model model, HttpServletResponse response, int status, String message) {
        response.setStatus(status);
        model.addAttribute("checkError", message);
        return "check";
    }
}
