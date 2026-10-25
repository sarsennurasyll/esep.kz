package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;
import com.esep.statementimport.exception.StatementFormatException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KaspiTransactionParserTest {

    private final KaspiTransactionParser transactionParser = new KaspiTransactionParser();

    @Test
    void shouldParseMagnumTransactionWithDefaultCurrency() {
        var transaction = transactionParser.parse("12.07.2026 MAGNUM CASH&CARRY -14500", 0);

        assertThat(transaction.date()).isEqualTo(LocalDate.of(2026, 7, 12));
        assertThat(transaction.description()).isEqualTo("MAGNUM CASH&CARRY");
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("-14500"));
        assertThat(transaction.currency()).isEqualTo("KZT");
    }

    @Test
    void shouldParseYandexTransaction() {
        var transaction = transactionParser.parse("13.07.2026 YANDEX.GO -2100", 0);

        assertThat(transaction.description()).isEqualTo("YANDEX.GO");
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("-2100"));
        assertThat(transaction.currency()).isEqualTo("KZT");
    }

    @Test
    void shouldParseEuropharmaTransactionWithCurrency() {
        var transaction = transactionParser.parse("14.07.2026 EUROPHARMA -4500 KZT", 0);

        assertThat(transaction.description()).isEqualTo("EUROPHARMA");
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("-4500"));
        assertThat(transaction.currency()).isEqualTo("KZT");
    }

    @Test
    void shouldParsePositiveAmount() {
        var transaction = transactionParser.parse("15.07.2026 CASHBACK 1 500,50 USD", 0);

        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("1500.50"));
        assertThat(transaction.currency()).isEqualTo("USD");
    }

    @Test
    void shouldParseKaspiPdfOperationLine() {
        var transaction = transactionParser.parse("26.07.26 - 4 670,00 ? Покупка YANDEX.GO", 0);

        assertThat(transaction.date()).isEqualTo(LocalDate.of(2026, 7, 26));
        assertThat(transaction.description()).isEqualTo("YANDEX.GO");
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("-4670.00"));
        assertThat(transaction.currency()).isEqualTo("KZT");
        assertThat(transaction.bankOperationType()).isEqualTo(BankOperationType.PURCHASE);
    }

    @Test
    void shouldParseTransferOperationTypeFromKaspiPdfLine() {
        var transaction = transactionParser.parse("26.07.26 - 1 000,00 ? Перевод Тестов Т.", 0);

        assertThat(transaction.bankOperationType()).isEqualTo(BankOperationType.TRANSFER);
    }

    @Test
    void shouldParseTopUpOperationTypeFromKaspiPdfLine() {
        var transaction = transactionParser.parse("26.07.26 + 5 000,00 ? Пополнение Тестова А.", 0);

        assertThat(transaction.bankOperationType()).isEqualTo(BankOperationType.TOP_UP);
    }

    @Test
    void shouldParseCashWithdrawalAndOtherOperationTypes() {
        var withdrawal = transactionParser.parse("30.08.26 - 9 000,00 ₸ Снятие Банкомат Тестовый", 0);
        var fee = transactionParser.parse("31.08.26 - 500,00 ₸ Разное Плата за годовое обслуживание", 1);

        assertThat(withdrawal.bankOperationType()).isEqualTo(BankOperationType.CASH_WITHDRAWAL);
        assertThat(withdrawal.description()).isEqualTo("Банкомат Тестовый");
        assertThat(fee.bankOperationType()).isEqualTo(BankOperationType.OTHER);
        assertThat(fee.amount()).isEqualByComparingTo(new BigDecimal("-500.00"));
    }

    @Test
    void shouldParseAmountWithMillions() {
        var transaction = transactionParser.parse("01.08.26 + 1 234 567,89 ₸ Пополнение Тестов Т.", 0);

        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("1234567.89"));
    }

    @Test
    void shouldRejectInvalidTransactionLineWithoutExposingItsContent() {
        assertThatThrownBy(() -> transactionParser.parse("MAGNUM -14500", 3))
                .isInstanceOf(StatementFormatException.class)
                .hasMessageContaining("3")
                .hasMessageNotContaining("MAGNUM")
                .hasMessageNotContaining("14500");
    }
}
