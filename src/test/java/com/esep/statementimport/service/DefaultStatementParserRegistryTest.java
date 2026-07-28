package com.esep.statementimport.service;

import com.esep.entity.BankType;
import com.esep.statementimport.exception.UnsupportedBankTypeException;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.model.ParsedStatement;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultStatementParserRegistryTest {

    @Test
    void shouldReturnKaspiParser() {
        StatementParser kaspiParser = parser(BankType.KASPI);
        DefaultStatementParserRegistry registry = new DefaultStatementParserRegistry(List.of(kaspiParser));

        assertThat(registry.getParser(BankType.KASPI)).isSameAs(kaspiParser);
    }

    @Test
    void shouldRejectBankWithoutRegisteredParser() {
        DefaultStatementParserRegistry registry = new DefaultStatementParserRegistry(List.of(parser(BankType.KASPI)));

        assertThatThrownBy(() -> registry.getParser(BankType.HALYK))
                .isInstanceOf(UnsupportedBankTypeException.class);
    }

    @Test
    void shouldRejectDuplicateParsersForSameBank() {
        assertThatThrownBy(() -> new DefaultStatementParserRegistry(List.of(
                parser(BankType.KASPI),
                parser(BankType.KASPI)
        ))).isInstanceOf(IllegalArgumentException.class);
    }

    private StatementParser parser(BankType bankType) {
        return new StatementParser() {
            @Override
            public BankType supportedBank() {
                return bankType;
            }

            @Override
            public ParsedStatement parse(InputStream input) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
