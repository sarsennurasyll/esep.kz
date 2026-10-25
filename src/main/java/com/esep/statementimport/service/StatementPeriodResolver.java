package com.esep.statementimport.service;

import com.esep.statementimport.exception.StatementWithoutTransactionsException;
import com.esep.statementimport.model.ParsedStatement;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * Определяет период выписки: берёт период из документа, а если parser его не нашёл, вычисляет по датам операций.
 */
public class StatementPeriodResolver {

    public StatementPeriod resolve(ParsedStatement statement) {
        if (statement.transactions().isEmpty()) {
            throw new StatementWithoutTransactionsException();
        }
        if (statement.periodFrom() != null && statement.periodTo() != null) {
            return new StatementPeriod(statement.periodFrom(), statement.periodTo());
        }

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
