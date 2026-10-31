package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.service.MerchantSuggestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Названия, присланные пользователями по согласию. Доступно только администратору (см. настройки безопасности).
 */
@Controller
@RequestMapping("/knowledge-base/suggestions")
public class MerchantSuggestionWebController {

    private final MerchantSuggestionService suggestions;

    public MerchantSuggestionWebController(MerchantSuggestionService suggestions) {
        this.suggestions = suggestions;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("suggestions", suggestions.findNew());
        return "knowledge-base-suggestions";
    }

    @PostMapping("/reject")
    public String reject(@RequestParam String name) {
        suggestions.reject(name);
        return "redirect:/knowledge-base/suggestions";
    }
}
