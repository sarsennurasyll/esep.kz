package com.esep.moneyflow.api;

import com.esep.moneyflow.api.dto.CounterpartyLabelingResponse;
import com.esep.moneyflow.api.dto.LabelingProgressResponse;
import com.esep.moneyflow.api.dto.PurposeRuleRequest;
import com.esep.moneyflow.api.dto.PurposeRuleResponse;
import com.esep.moneyflow.service.PurposeLabelingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP API личной разметки назначения операций по контрагенту.
 */
@RestController
@RequestMapping("/api/purposes")
@Tag(name = "Purposes", description = "Назначение переводов и поступлений, известное только пользователю")
public class PurposeLabelingController {

    private final PurposeLabelingService purposeLabelingService;

    public PurposeLabelingController(PurposeLabelingService purposeLabelingService) {
        this.purposeLabelingService = purposeLabelingService;
    }

    @GetMapping("/counterparties")
    @Operation(summary = "Контрагенты для разметки, по убыванию суммы")
    public List<CounterpartyLabelingResponse> findCounterparties() {
        return purposeLabelingService.findCounterparties().stream().map(CounterpartyLabelingResponse::from).toList();
    }

    @GetMapping("/progress")
    @Operation(summary = "Доля суммы переводов и поступлений с известным назначением")
    public LabelingProgressResponse progress() {
        return LabelingProgressResponse.from(purposeLabelingService.progress());
    }

    @PutMapping("/rules")
    @Operation(summary = "Создать или изменить правило назначения для контрагента")
    public PurposeRuleResponse setRule(@Valid @RequestBody PurposeRuleRequest request) {
        return PurposeRuleResponse.from(purposeLabelingService.setRule(request.counterpartyKey(), request.direction(), request.purpose()));
    }

    @DeleteMapping("/rules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить правило; операции снова получают назначение «неизвестно»")
    public void removeRule(@PathVariable long id) {
        purposeLabelingService.removeRule(id);
    }
}
