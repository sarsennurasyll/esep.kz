package com.esep.moneyflow.api.dto;

import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Запрос на создание или изменение личного правила назначения. */
public record PurposeRuleRequest(
        @NotBlank @Size(max = 1_000) String counterpartyKey,
        @NotNull MoneyDirection direction,
        @NotNull MoneyPurpose purpose
) {
}
