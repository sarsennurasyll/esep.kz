package com.esep.moneyflow.api.dto;

import com.esep.moneyflow.model.CounterpartyPurposeRule;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;

/** HTTP-представление личного правила назначения. */
public record PurposeRuleResponse(long id, String counterpartyKey, MoneyDirection direction, MoneyPurpose purpose) {

    public static PurposeRuleResponse from(CounterpartyPurposeRule rule) {
        return rule == null ? null : new PurposeRuleResponse(rule.id(), rule.counterpartyKey(), rule.direction(), rule.purpose());
    }
}
