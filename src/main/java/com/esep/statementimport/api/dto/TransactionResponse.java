package com.esep.statementimport.api.dto;

import com.esep.entity.TransactionType;
import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.statementimport.query.model.TransactionQueryResult;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * HTTP-представление операции импортированной выписки.
 */
public record TransactionResponse(
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

    public static TransactionResponse from(TransactionQueryResult result) {
        return new TransactionResponse(
                result.date(),
                result.description(),
                result.merchant(),
                result.category(),
                result.amount(),
                result.currency(),
                result.transactionType(),
                result.id(),
                result.flowType(),
                result.purpose(),
                result.purposeSource()
        );
    }
}
