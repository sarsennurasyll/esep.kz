package com.esep.moneyflow.api.dto;

import com.esep.moneyflow.model.MoneyPurpose;
import jakarta.validation.constraints.NotNull;

/** Запрос на ручное назначение отдельной операции. */
public record OperationPurposeRequest(@NotNull MoneyPurpose purpose) {
}
