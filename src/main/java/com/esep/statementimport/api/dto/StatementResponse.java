package com.esep.statementimport.api.dto;

import com.esep.entity.BankType;
import com.esep.statementimport.query.model.StatementQueryResult;

import java.time.Instant;
import java.time.LocalDate;

/**
 * HTTP-представление импортированной выписки.
 */
public record StatementResponse(
        Long id,
        BankType bank,
        String originalFileName,
        LocalDate periodFrom,
        LocalDate periodTo,
        long transactionCount,
        Instant importedAt
) {

    public static StatementResponse from(StatementQueryResult result) {
        return new StatementResponse(
                result.id(),
                result.bank(),
                result.originalFileName(),
                result.periodFrom(),
                result.periodTo(),
                result.transactionCount(),
                result.importedAt()
        );
    }
}
