package com.esep.statementimport.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Неизменяемая модель банковской выписки, созданная парсером.
 */
public record ParsedStatement(
        String accountNumber,
        LocalDate periodFrom,
        LocalDate periodTo,
        List<ParsedTransaction> transactions
) {

    public ParsedStatement {
        transactions = List.copyOf(transactions);
    }
}
