package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;
import com.esep.statementimport.exception.StatementFormatException;
import com.esep.statementimport.model.ParsedTransaction;
import com.esep.statementimport.pdf.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KaspiStatementParserTest {

    // Синтетическая выписка в структуре реального Kaspi Gold PDF: шапка с периодом и итогами, все типы операций.
    private static final String GOLD_STATEMENT = """
            АО «Kaspi Bank», БИК CASPKZKA, www.kaspi.kz
            ВЫПИСКА
            по Kaspi Gold за период с 01.08.26 по 31.08.26
            Тестов Клиент
            Номер карты: *0000
            Доступно на 31.08.26 + 10 000,00 ₸ Валюта счета: тенге
            Краткое содержание операций по карте:
            Доступно на 01.08.26 + 1 000,00 ₸ Остаток зарплатных денег 0,00 ₸
            Пополнения + 60 000,00 ₸ Кредитный лимит 0,00 ₸
            Переводы - 20 000,00 ₸
            Переводы на Kaspi Депозит + 0,00 ₸
            Покупки - 21 500,00 ₸
            Снятия - 9 000,00 ₸
            Разное - 500,00 ₸
            Доступно на 31.08.26 + 10 000,00 ₸
            Дата Сумма Операция Детали
            31.08.26 - 500,00 ₸ Разное Плата за годовое обслуживание
            30.08.26 - 9 000,00 ₸ Снятие Банкомат Тестовый
            29.08.26 - 3 000,00 ₸ Покупка TEST DIGITAL SERVICE
            (- 6,00 USD)
            28.08.26 + 1 500,00 ₸ Покупка ТЕСТОВЫЙ МАГАЗИН
            15.08.26 - 20 000,00 ₸ Покупка ТЕСТОВЫЙ МАГАЗИН
            10.08.26 - 20 000,00 ₸ Перевод Тестов Т.
            05.08.26 + 10 000,00 ₸ Пополнение В Kaspi Банкомате
            01.08.26 + 50 000,00 ₸ Пополнение Тестов Т.
            АО «Kaspi Bank», БИК CASPKZKA, www.kaspi.kz
            """;

    @Test
    void shouldCreateParsedTransactionsFromExtractedOperationLines() {
        PdfTextExtractor pdfTextExtractor = input -> """
                Выписка Kaspi
                Операции
                12.07.2026 MAGNUM CASH&CARRY -14500
                13.07.2026 YANDEX.GO -2100
                """;
        var parser = new KaspiStatementParser(pdfTextExtractor);

        var statement = parser.parse(new ByteArrayInputStream(new byte[0]));

        assertThat(statement.transactions()).hasSize(2);
        assertThat(statement.transactions().getFirst().description()).isEqualTo("MAGNUM CASH&CARRY");
        assertThat(statement.transactions().getFirst().amount()).isEqualByComparingTo(new BigDecimal("-14500"));
        assertThat(statement.transactions().getFirst().sourceRecordPosition()).isZero();
        assertThat(statement.transactions().get(1).description()).isEqualTo("YANDEX.GO");
        assertThat(statement.transactions().get(1).sourceRecordPosition()).isEqualTo(1);
    }

    @Test
    void shouldParseEveryKaspiGoldOperationType() {
        var statement = parse(GOLD_STATEMENT);

        assertThat(statement.transactions()).extracting(ParsedTransaction::bankOperationType).containsExactly(
                BankOperationType.OTHER,
                BankOperationType.CASH_WITHDRAWAL,
                BankOperationType.PURCHASE,
                BankOperationType.PURCHASE,
                BankOperationType.PURCHASE,
                BankOperationType.TRANSFER,
                BankOperationType.TOP_UP,
                BankOperationType.TOP_UP
        );
        assertThat(statement.transactions()).extracting(ParsedTransaction::sourceRecordPosition)
                .containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
    }

    @Test
    void shouldKeepRefundSignAndAccountCurrencyAmountForForeignCurrencyPurchase() {
        var statement = parse(GOLD_STATEMENT);

        ParsedTransaction foreignCurrencyPurchase = statement.transactions().get(2);
        assertThat(foreignCurrencyPurchase.description()).isEqualTo("TEST DIGITAL SERVICE");
        assertThat(foreignCurrencyPurchase.amount()).isEqualByComparingTo("-3000.00");
        assertThat(foreignCurrencyPurchase.currency()).isEqualTo("KZT");
        assertThat(statement.transactions().get(3).amount()).isEqualByComparingTo("1500.00");
    }

    @Test
    void shouldReadStatementPeriodFromHeader() {
        var statement = parse(GOLD_STATEMENT);

        assertThat(statement.periodFrom()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(statement.periodTo()).isEqualTo(LocalDate.of(2026, 8, 31));
    }

    @Test
    void shouldReadOnlyCardNumberSuffixFromHeader() {
        var statement = parse(GOLD_STATEMENT);

        assertThat(statement.accountNumber()).isEqualTo("0000");
    }

    @Test
    void shouldRejectStatementWhenOperationsDoNotMatchHeaderTotals() {
        String statementWithMissingOperation = GOLD_STATEMENT.replace(
                "30.08.26 - 9 000,00 ₸ Снятие Банкомат Тестовый\n", ""
        );

        assertThatThrownBy(() -> parse(statementWithMissingOperation))
                .isInstanceOf(StatementFormatException.class)
                .hasMessageContaining("CASH_WITHDRAWAL")
                .hasMessageNotContaining("9 000")
                .hasMessageNotContaining("Банкомат");
    }

    @Test
    void shouldRejectOperationOutsideStatementPeriod() {
        String statementWithForeignDate = GOLD_STATEMENT.replace("01.08.26 + 50 000,00", "15.09.26 + 50 000,00");

        assertThatThrownBy(() -> parse(statementWithForeignDate))
                .isInstanceOf(StatementFormatException.class)
                .hasMessageNotContaining("Тестов");
    }

    @Test
    void shouldMarkUnknownOperationTypeInsteadOfRejectingStatement() {
        var statement = parse("""
                Дата Сумма Операция Детали
                05.08.26 + 300,00 ₸ Бонусы Начисление за покупки
                04.08.26 - 1 000,00 ₸ Покупка ТЕСТОВЫЙ МАГАЗИН
                """);

        ParsedTransaction unknownOperation = statement.transactions().getFirst();
        assertThat(unknownOperation.bankOperationType()).isEqualTo(BankOperationType.UNKNOWN);
        assertThat(unknownOperation.description()).isEqualTo("Бонусы Начисление за покупки");
        assertThat(unknownOperation.amount()).isEqualByComparingTo("300.00");
    }

    @Test
    void shouldNotExposeOperationDetailsWhenLineCannotBeParsed() {
        assertThatThrownBy(() -> parse("""
                Дата Сумма Операция Детали
                05.08.26 Тестов Т. без суммы
                """))
                .isInstanceOf(StatementFormatException.class)
                .hasMessageNotContaining("Тестов")
                .hasMessageNotContaining("05.08.26");
    }

    private com.esep.statementimport.model.ParsedStatement parse(String text) {
        return new KaspiStatementParser(input -> text).parse(new ByteArrayInputStream(new byte[0]));
    }
}
