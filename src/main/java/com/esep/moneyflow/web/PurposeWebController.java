package com.esep.moneyflow.web;

import com.esep.moneyflow.exception.OperationNotFoundException;
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

    @GetMapping("/counterparty")
    public String counterpartyOperations(@RequestParam String key, @RequestParam MoneyDirection direction, Model model) {
        model.addAttribute("counterpartyKey", key);
        model.addAttribute("direction", direction);
        model.addAttribute("operations", purposeLabelingService.findCounterpartyOperations(key, direction));
        model.addAttribute("purposeOptions", optionsFor(direction));
        return "purpose-counterparty";
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

    // Возврат только на страницу выписки по числовому идентификатору, чтобы адрес перенаправления нельзя было подменить.
    @PostMapping("/operations/{id}")
    public String setOperationPurpose(
            @PathVariable long id,
            @RequestParam MoneyPurpose purpose,
            @RequestParam(required = false) Long statementId,
            @RequestParam(required = false) String counterpartyKey,
            @RequestParam(required = false) MoneyDirection direction,
            RedirectAttributes attributes
    ) {
        try {
            purposeLabelingService.setOperationPurpose(id, purpose);
            attributes.addFlashAttribute("successMessage", "Назначение операции сохранено. Правило контрагента для неё больше не действует.");
        } catch (IllegalArgumentException | OperationNotFoundException exception) {
            attributes.addFlashAttribute("errorMessage", "Это назначение нельзя выбрать для операции.");
        }
        return returnTarget(statementId, counterpartyKey, direction, attributes);
    }

    @PostMapping("/operations/{id}/clear")
    public String clearOperationPurpose(
            @PathVariable long id,
            @RequestParam(required = false) Long statementId,
            @RequestParam(required = false) String counterpartyKey,
            @RequestParam(required = false) MoneyDirection direction,
            RedirectAttributes attributes
    ) {
        try {
            purposeLabelingService.clearOperationPurpose(id);
            attributes.addFlashAttribute("successMessage", "Ручное назначение отменено: действует правило контрагента или «не знаю».");
        } catch (IllegalArgumentException | OperationNotFoundException exception) {
            attributes.addFlashAttribute("errorMessage", "Операция не найдена.");
        }
        return returnTarget(statementId, counterpartyKey, direction, attributes);
    }

    /**
     * Возврат туда, откуда пришла форма: к выписке или к операциям контрагента. Адрес собирается только
     * из числового id и параметров запроса, поэтому перенаправить на внешний сайт нельзя.
     */
    private String returnTarget(Long statementId, String counterpartyKey, MoneyDirection direction, RedirectAttributes attributes) {
        if (statementId != null) {
            return "redirect:/statements/" + statementId;
        }
        if (counterpartyKey != null && direction != null) {
            attributes.addAttribute("key", counterpartyKey);
            attributes.addAttribute("direction", direction);
            return "redirect:/purposes/counterparty";
        }
        return "redirect:/purposes";
    }

    private List<MoneyPurpose> optionsFor(MoneyDirection direction) {
        return direction.knownPurposes();
    }
}
