package com.esep.statementimport.query.model;

import com.esep.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Данные операции для сценария просмотра выписки.
 */
public record TransactionQueryResult(
        LocalDate date,
        String description,
        String merchant,
        String category,
        BigDecimal amount,
        String currency,
        TransactionType transactionType
) {
}
