package com.esep.moneyflow.api.dto;

import com.esep.moneyflow.model.CounterpartyLabelingItem;
import com.esep.moneyflow.model.MoneyDirection;

import java.math.BigDecimal;

/** HTTP-представление контрагента для разметки назначения. */
public record CounterpartyLabelingResponse(
        String counterpartyKey,
        String exampleDescription,
        MoneyDirection direction,
        BigDecimal amount,
        long operationCount,
        BigDecimal labeledAmount,
        long manualOperationCount,
        PurposeRuleResponse rule
) {

    public static CounterpartyLabelingResponse from(CounterpartyLabelingItem item) {
        return new CounterpartyLabelingResponse(item.counterpartyKey(), item.exampleDescription(), item.direction(),
                item.amount(), item.operationCount(), item.labeledAmount(), item.manualOperationCount(),
                PurposeRuleResponse.from(item.rule()));
    }
}
