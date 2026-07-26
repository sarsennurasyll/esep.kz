package com.esep.statementimport.kaspi;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KaspiTransactionParserTest {

    private final KaspiTransactionParser transactionParser = new KaspiTransactionParser();

    @Test
    void shouldParseMagnumTransactionWithDefaultCurrency() {
        var transaction = transactionParser.parse("12.07.2026 MAGNUM CASH&CARRY -14500");

        assertThat(transaction.date()).isEqualTo(LocalDate.of(2026, 7, 12));
        assertThat(transaction.description()).isEqualTo("MAGNUM CASH&CARRY");
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("-14500"));
        assertThat(transaction.currency()).isEqualTo("KZT");
    }

    @Test
    void shouldParseYandexTransaction() {
        var transaction = transactionParser.parse("13.07.2026 YANDEX.GO -2100");

        assertThat(transaction.description()).isEqualTo("YANDEX.GO");
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("-2100"));
        assertThat(transaction.currency()).isEqualTo("KZT");
    }

    @Test
    void shouldParseEuropharmaTransactionWithCurrency() {
        var transaction = transactionParser.parse("14.07.2026 EUROPHARMA -4500 KZT");

        assertThat(transaction.description()).isEqualTo("EUROPHARMA");
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("-4500"));
        assertThat(transaction.currency()).isEqualTo("KZT");
    }

    @Test
    void shouldParsePositiveAmount() {
        var transaction = transactionParser.parse("15.07.2026 CASHBACK 1 500,50 USD");

        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("1500.50"));
        assertThat(transaction.currency()).isEqualTo("USD");
    }

    @Test
    void shouldRejectInvalidTransactionLine() {
        assertThatThrownBy(() -> transactionParser.parse("MAGNUM -14500"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
