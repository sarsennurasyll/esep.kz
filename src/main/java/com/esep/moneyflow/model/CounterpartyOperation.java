package com.esep.moneyflow.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Операция контрагента с назначением, которое пользователь может изменить вручную. */
public record CounterpartyOperation(
        long id,
        long statementId,
        LocalDate date,
        String description,
        BigDecimal amount,
        MoneyPurpose purpose,
        ClassificationSource purposeSource
) {
}
