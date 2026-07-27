package com.esep.statementimport.service;

import com.esep.statementimport.exception.StatementWithoutTransactionsException;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatementPeriodResolverTest {

    private final StatementPeriodResolver resolver = new StatementPeriodResolver();

    @Test
    void shouldResolvePeriodFromTransactionDates() {
        ParsedStatement statement = new ParsedStatement(
                null,
                null,
                null,
                List.of(transaction(LocalDate.of(2026, 7, 14)), transaction(LocalDate.of(2026, 7, 2)))
        );

        StatementPeriod period = resolver.resolve(statement);

        assertThat(period.periodFrom()).isEqualTo(LocalDate.of(2026, 7, 2));
        assertThat(period.periodTo()).isEqualTo(LocalDate.of(2026, 7, 14));
    }

    @Test
    void shouldRejectStatementWithoutTransactions() {
        ParsedStatement statement = new ParsedStatement(null, null, null, List.of());

        assertThatThrownBy(() -> resolver.resolve(statement))
                .isInstanceOf(StatementWithoutTransactionsException.class);
    }

    private ParsedTransaction transaction(LocalDate date) {
        return new ParsedTransaction(date, "MAGNUM", BigDecimal.ONE, "KZT");
    }
}
