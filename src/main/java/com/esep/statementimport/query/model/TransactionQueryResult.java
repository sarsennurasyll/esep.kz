package com.esep.statementimport.query.model;

import com.esep.entity.TransactionType;
import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;

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
        TransactionType transactionType,
        Long id,
        MoneyFlowType flowType,
        MoneyPurpose purpose,
        ClassificationSource purposeSource
) {
}
