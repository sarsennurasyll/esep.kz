package com.esep.statementimport.kaspi;

import com.esep.statementimport.model.ParsedTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Преобразует одну сырую строку операции Kaspi в структурированную операцию.
 */
class KaspiTransactionParser {

    private static final String DEFAULT_CURRENCY = "KZT";
    private static final DateTimeFormatter FULL_YEAR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uuuu");
    private static final DateTimeFormatter SHORT_YEAR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uu");
    private static final Pattern NORMALIZED_TRANSACTION_LINE = Pattern.compile(
            "^(?<date>\\d{2}\\.\\d{2}\\.\\d{4})\\s+"
                    + "(?<description>.+?)\\s+"
                    + "(?<amount>[+-]?\\s?(?:\\d{1,3}(?:\\s\\d{3})*|\\d+)(?:[.,]\\d+)?)"
                    + "(?:\\s+(?<currency>[A-Za-z]{3}))?$"
    );
    private static final Pattern KASPI_TRANSACTION_LINE = Pattern.compile(
            "^(?<date>\\d{2}\\.\\d{2}\\.\\d{2})\\s+"
                    + "(?<amount>[+-]?\\s?(?:\\d{1,3}(?:\\s\\d{3})*|\\d+)(?:[.,]\\d+)?)"
                    + "(?:\\s+(?:[^\\p{L}\\p{N}\\s]+|[A-Za-z]{3}))?"
                    + "\\s+(?:Покупка|Пополнение|Перевод)\\s+"
                    + "(?<description>.+)$"
    );

    ParsedTransaction parse(String rawLine) {
        if (rawLine == null) {
            throw new IllegalArgumentException("Строка операции не должна быть null.");
        }

        String normalizedLine = rawLine.strip();
        Matcher normalizedMatcher = NORMALIZED_TRANSACTION_LINE.matcher(normalizedLine);
        if (normalizedMatcher.matches()) {
            return createTransaction(
                    normalizedMatcher,
                    FULL_YEAR_DATE_FORMATTER,
                    normalizedMatcher.group("currency")
            );
        }

        Matcher kaspiMatcher = KASPI_TRANSACTION_LINE.matcher(normalizedLine);
        if (kaspiMatcher.matches()) {
            return createTransaction(kaspiMatcher, SHORT_YEAR_DATE_FORMATTER, null);
        }

        throw new IllegalArgumentException("Некорректный формат строки операции: " + rawLine);
    }

    private ParsedTransaction createTransaction(
            Matcher matcher,
            DateTimeFormatter dateFormatter,
            String currency
    ) {
        try {
            LocalDate date = LocalDate.parse(matcher.group("date"), dateFormatter);
            String description = matcher.group("description");
            BigDecimal amount = new BigDecimal(normalizeAmount(matcher.group("amount")));
            String resolvedCurrency = resolveCurrency(currency);

            return new ParsedTransaction(date, description, amount, resolvedCurrency);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Некорректная дата операции: " + matcher.group("date"), exception);
        }
    }

    private String normalizeAmount(String amount) {
        return amount.replace(" ", "").replace(',', '.');
    }

    private String resolveCurrency(String currency) {
        return currency == null ? DEFAULT_CURRENCY : currency.toUpperCase(Locale.ROOT);
    }
}
