package com.esep.statementimport.web;

import com.esep.entity.BankType;
import com.esep.statementimport.api.dto.StatementImportResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC-контроллер страниц импорта и просмотра выписок.
 */
@Controller
@RequestMapping
public class StatementWebController {

    private final StatementWebApiClient statementWebApiClient;

    public StatementWebController(StatementWebApiClient statementWebApiClient) {
        this.statementWebApiClient = statementWebApiClient;
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
        return "statement-details";
    }

    private String importErrorMessage(StatementApiException exception) {
        return switch (exception.getStatusCode()) {
            case 400 -> "Не удалось импортировать выписку. Проверьте, что выбран корректный PDF-файл Kaspi.";
            case 409 -> "Эта выписка уже была импортирована.";
            default -> "Не удалось импортировать выписку. Попробуйте ещё раз позже.";
        };
    }
}
