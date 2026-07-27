package com.esep.statementimport.query.model;

import com.esep.entity.BankType;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Данные выписки для сценариев чтения.
 */
public record StatementQueryResult(
        Long id,
        BankType bank,
        String originalFileName,
        LocalDate periodFrom,
        LocalDate periodTo,
        long transactionCount,
        Instant importedAt
) {
}
