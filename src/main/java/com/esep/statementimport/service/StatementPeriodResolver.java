package com.esep.statementimport.service;

import com.esep.statementimport.exception.StatementWithoutTransactionsException;
import com.esep.statementimport.model.ParsedStatement;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * Вычисляет период выписки по датам извлечённых операций.
 */
public class StatementPeriodResolver {

    public StatementPeriod resolve(ParsedStatement statement) {
        LocalDate periodFrom = statement.transactions().stream()
                .map(transaction -> transaction.date())
                .min(Comparator.naturalOrder())
                .orElseThrow(StatementWithoutTransactionsException::new);
        LocalDate periodTo = statement.transactions().stream()
                .map(transaction -> transaction.date())
                .max(Comparator.naturalOrder())
                .orElseThrow(StatementWithoutTransactionsException::new);

        return new StatementPeriod(periodFrom, periodTo);
    }
}
