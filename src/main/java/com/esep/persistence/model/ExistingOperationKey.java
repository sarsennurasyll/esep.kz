package com.esep.persistence.model;

import com.esep.entity.BankOperationType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Признаки уже сохранённой операции, по которым новая выписка того же счёта сравнивается с прежними.
 */
public record ExistingOperationKey(
        LocalDate date,
        BigDecimal amount,
        BankOperationType bankOperationType,
        String description
) {
}
