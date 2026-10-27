package com.esep.moneyflow.model;

import java.math.BigDecimal;

/** Операция, назначение которой пользователь задаёт вручную. */
public record LabelableOperation(long id, String description, BigDecimal amount, MoneyFlowType flowType) {
}
