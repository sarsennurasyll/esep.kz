package com.esep.merchantmanagement.web;

import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseAliasRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/knowledge-base/merchants")
public class KnowledgeBaseWebController {

    private final KnowledgeBaseWebApiClient knowledgeBaseApiClient;

    public KnowledgeBaseWebController(KnowledgeBaseWebApiClient knowledgeBaseApiClient) {
        this.knowledgeBaseApiClient = knowledgeBaseApiClient;
    }

    @GetMapping
    public String merchants(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) MerchantType merchantType,
            @RequestParam(required = false) String categoryCode,
            Model model
    ) {
        model.addAttribute("merchants", knowledgeBaseApiClient.findMerchants(query, merchantType, categoryCode));
        model.addAttribute("categories", knowledgeBaseApiClient.findCategories());
        model.addAttribute("merchantTypes", MerchantType.values());
        model.addAttribute("query", query);
        model.addAttribute("selectedType", merchantType);
        model.addAttribute("selectedCategory", categoryCode);
        return "knowledge-base-merchants";
    }

    @PostMapping
    public String createMerchant(
            @RequestParam String name, @RequestParam MerchantType merchantType, @RequestParam String categoryCode,
            RedirectAttributes attributes
    ) {
        try {
            String id = knowledgeBaseApiClient.createMerchant(new KnowledgeBaseMerchantRequest(name, merchantType, categoryCode)).id();
            attributes.addFlashAttribute("successMessage", "Продавец добавлен в базу знаний.");
            return "redirect:/knowledge-base/merchants/" + id;
        } catch (MerchantApiException exception) {
            attributes.addFlashAttribute("errorMessage", "Не удалось создать продавца. Проверьте название и категорию.");
            return "redirect:/knowledge-base/merchants";
        }
    }

    @GetMapping("/{id}")
    public String merchant(@PathVariable String id, Model model) {
        model.addAttribute("merchant", knowledgeBaseApiClient.findMerchant(id));
        model.addAttribute("categories", knowledgeBaseApiClient.findCategories());
        model.addAttribute("merchantTypes", MerchantType.values());
        return "knowledge-base-merchant";
    }

    @PostMapping("/{id}")
    public String updateMerchant(@PathVariable String id, @RequestParam String name, @RequestParam MerchantType merchantType,
                                 @RequestParam String categoryCode, RedirectAttributes attributes) {
        try {
            knowledgeBaseApiClient.updateMerchant(id, new KnowledgeBaseMerchantRequest(name, merchantType, categoryCode));
            attributes.addFlashAttribute("successMessage", "Карточка продавца обновлена.");
        } catch (MerchantApiException exception) {
            attributes.addFlashAttribute("errorMessage", "Не удалось обновить карточку продавца.");
        }
        return "redirect:/knowledge-base/merchants/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteMerchant(@PathVariable String id, RedirectAttributes attributes) {
        try {
            knowledgeBaseApiClient.deleteMerchant(id);
            attributes.addFlashAttribute("successMessage", "Продавец удалён.");
            return "redirect:/knowledge-base/merchants";
        } catch (MerchantApiException exception) {
            attributes.addFlashAttribute("errorMessage", "Удаление недоступно: у продавца есть операции или алиасы. Сначала измените их.");
            return "redirect:/knowledge-base/merchants/" + id;
        }
    }

    @PostMapping("/{id}/aliases")
    public String addAlias(@PathVariable String id, @RequestParam String aliasName, RedirectAttributes attributes) {
        try {
            knowledgeBaseApiClient.addAlias(id, new KnowledgeBaseAliasRequest(aliasName));
            attributes.addFlashAttribute("successMessage", "Алиас добавлен. Все совпадающие неизвестные операции связаны с продавцом.");
        } catch (MerchantApiException exception) {
            attributes.addFlashAttribute("errorMessage", "Не удалось добавить алиас: он пустой или уже существует.");
        }
        return "redirect:/knowledge-base/merchants/" + id;
    }

    @PostMapping("/{id}/aliases/{aliasId}")
    public String updateAlias(@PathVariable String id, @PathVariable Long aliasId, @RequestParam String aliasName, RedirectAttributes attributes) {
        try {
            knowledgeBaseApiClient.updateAlias(id, aliasId, new KnowledgeBaseAliasRequest(aliasName));
            attributes.addFlashAttribute("successMessage", "Алиас обновлён.");
        } catch (MerchantApiException exception) {
            attributes.addFlashAttribute("errorMessage", "Не удалось обновить алиас.");
        }
        return "redirect:/knowledge-base/merchants/" + id;
    }

    @PostMapping("/{id}/aliases/{aliasId}/delete")
    public String deleteAlias(@PathVariable String id, @PathVariable Long aliasId, RedirectAttributes attributes) {
        try {
            knowledgeBaseApiClient.deleteAlias(id, aliasId);
            attributes.addFlashAttribute("successMessage", "Алиас удалён.");
        } catch (MerchantApiException exception) {
            attributes.addFlashAttribute("errorMessage", "Не удалось удалить алиас.");
        }
        return "redirect:/knowledge-base/merchants/" + id;
    }
}
