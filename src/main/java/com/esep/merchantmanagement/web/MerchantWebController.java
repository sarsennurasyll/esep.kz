package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.api.dto.MerchantMatchRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC-контроллер страницы связывания неизвестных продавцов.
 */
@Controller
@RequestMapping("/merchants")
public class MerchantWebController {

    private final MerchantWebApiClient merchantWebApiClient;

    public MerchantWebController(MerchantWebApiClient merchantWebApiClient) {
        this.merchantWebApiClient = merchantWebApiClient;
    }

    @GetMapping
    public String merchants(Model model) {
        model.addAttribute("unknownDescriptions", merchantWebApiClient.findUnknownDescriptions());
        model.addAttribute("merchants", merchantWebApiClient.findMerchants());
        return "merchants";
    }

    @PostMapping("/match")
    public String match(
            @RequestParam String normalizedDescription,
            @RequestParam String merchantId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            merchantWebApiClient.match(new MerchantMatchRequest(normalizedDescription, merchantId));
            redirectAttributes.addFlashAttribute("successMessage", "Соответствие сохранено.");
        } catch (MerchantApiException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/merchants";
    }

    private String errorMessage(MerchantApiException exception) {
        return switch (exception.getStatusCode()) {
            case 404 -> "Выбранный продавец не найден.";
            case 409 -> "Это соответствие уже существует.";
            case 400 -> "Не удалось сохранить соответствие. Проверьте данные.";
            default -> "Не удалось сохранить соответствие. Попробуйте ещё раз позже.";
        };
    }
}
