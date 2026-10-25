package com.esep.moneyflow.web;

import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.moneyflow.service.PurposeLabelingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Страница разметки назначения переводов и поступлений по контрагенту.
 */
@Controller
@RequestMapping("/purposes")
public class PurposeWebController {

    private final PurposeLabelingService purposeLabelingService;

    public PurposeWebController(PurposeLabelingService purposeLabelingService) {
        this.purposeLabelingService = purposeLabelingService;
    }

    @GetMapping
    public String purposes(Model model) {
        model.addAttribute("counterparties", purposeLabelingService.findCounterparties());
        model.addAttribute("progress", purposeLabelingService.progress());
        model.addAttribute("purposeOptions", Map.of(
                MoneyDirection.INFLOW, optionsFor(MoneyDirection.INFLOW),
                MoneyDirection.OUTFLOW, optionsFor(MoneyDirection.OUTFLOW)
        ));
        return "purposes";
    }

    @PostMapping("/rules")
    public String setRule(
            @RequestParam String counterpartyKey,
            @RequestParam MoneyDirection direction,
            @RequestParam MoneyPurpose purpose,
            RedirectAttributes attributes
    ) {
        try {
            purposeLabelingService.setRule(counterpartyKey, direction, purpose);
            attributes.addFlashAttribute("successMessage", "Назначение сохранено для всех операций с этим контрагентом.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", "Это назначение нельзя выбрать для такого направления.");
        }
        return "redirect:/purposes";
    }

    @PostMapping("/rules/{id}/delete")
    public String removeRule(@PathVariable long id, RedirectAttributes attributes) {
        try {
            purposeLabelingService.removeRule(id);
            attributes.addFlashAttribute("successMessage", "Правило удалено, назначение операций снова «не знаю».");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", "Правило не найдено.");
        }
        return "redirect:/purposes";
    }

    private List<MoneyPurpose> optionsFor(MoneyDirection direction) {
        return Arrays.stream(MoneyPurpose.values())
                .filter(purpose -> purpose != MoneyPurpose.UNKNOWN && direction.allows(purpose))
                .toList();
    }
}
