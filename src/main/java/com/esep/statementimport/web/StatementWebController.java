package com.esep.statementimport.web;

import com.esep.entity.BankType;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.statementimport.api.dto.StatementImportResponse;
import com.esep.statementimport.exception.StatementDeletionMismatchException;
import com.esep.statementimport.exception.StatementNotFoundException;
import com.esep.statementimport.model.StatementDeletionPlan;
import com.esep.statementimport.service.StatementDeletionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * MVC-контроллер страниц импорта и просмотра выписок.
 */
@Controller
@RequestMapping
public class StatementWebController {

    private final StatementWebApiClient statementWebApiClient;
    private final StatementDeletionService statementDeletionService;

    public StatementWebController(StatementWebApiClient statementWebApiClient, StatementDeletionService statementDeletionService) {
        this.statementWebApiClient = statementWebApiClient;
        this.statementDeletionService = statementDeletionService;
    }

    @GetMapping("/import")
    public String importForm() {
        return "import";
    }

    @PostMapping("/import")
    public String importStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "bankType", defaultValue = "KASPI") BankType bankType,
            RedirectAttributes redirectAttributes
    ) {
        try {
            StatementImportResponse result = statementWebApiClient.importStatement(file, bankType);
            redirectAttributes.addFlashAttribute("importReport", result);
            return "redirect:/statements/" + result.statementId();
        } catch (StatementApiException exception) {
            redirectAttributes.addFlashAttribute("importError", importErrorMessage(exception));
            return "redirect:/import";
        }
    }

    @GetMapping("/statements")
    public String statements(Model model) {
        model.addAttribute("statements", statementWebApiClient.findAllStatements());
        return "statements";
    }

    @GetMapping("/statements/{id}")
    public String statement(@PathVariable Long id, Model model) {
        model.addAttribute("statement", statementWebApiClient.findStatementById(id));
        model.addAttribute("transactions", statementWebApiClient.findTransactionsByStatementId(id));
        model.addAttribute("purposeOptions", Map.of(
                MoneyDirection.INFLOW.name(), MoneyDirection.INFLOW.knownPurposes(),
                MoneyDirection.OUTFLOW.name(), MoneyDirection.OUTFLOW.knownPurposes()
        ));
        model.addAttribute("deletionPlan", statementDeletionService.plan(id));
        return "statement-details";
    }

    /** Удаляет ровно тот набор выписок, который пользователь видел на странице подтверждения. */
    @PostMapping("/statements/{id}/delete")
    public String deleteStatement(
            @PathVariable long id,
            @RequestParam(name = "statementIds") List<Long> statementIds,
            RedirectAttributes redirectAttributes
    ) {
        try {
            StatementDeletionPlan deleted = statementDeletionService.delete(id, statementIds);
            redirectAttributes.addFlashAttribute("successMessage", "Удалено выписок: " + deleted.statementIds().size()
                    + ", операций: " + deleted.operationCount() + ". Правила назначения контрагентов сохранены; их можно удалить на странице «Назначение».");
            return "redirect:/statements";
        } catch (StatementDeletionMismatchException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Набор выписок для удаления изменился. Проверьте его ещё раз и подтвердите удаление.");
            return "redirect:/statements/" + id;
        } catch (StatementNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Выписка уже удалена.");
            return "redirect:/statements";
        }
    }

    private String importErrorMessage(StatementApiException exception) {
        return StatementErrorMessages.of(exception.getStatusCode(), exception.getErrorCode());
    }
}
