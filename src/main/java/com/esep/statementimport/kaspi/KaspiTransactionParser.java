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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uuuu");
    private static final Pattern TRANSACTION_LINE = Pattern.compile(
            "^(?<date>\\d{2}\\.\\d{2}\\.\\d{4})\\s+"
                    + "(?<description>.+?)\\s+"
                    + "(?<amount>[+-]?(?:\\d{1,3}(?:\\s\\d{3})*|\\d+)(?:[.,]\\d+)?)"
                    + "(?:\\s+(?<currency>[A-Za-z]{3}))?$"
    );

    ParsedTransaction parse(String rawLine) {
        if (rawLine == null) {
            throw new IllegalArgumentException("Строка операции не должна быть null.");
        }

        Matcher matcher = TRANSACTION_LINE.matcher(rawLine.strip());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Некорректный формат строки операции: " + rawLine);
        }

        try {
            LocalDate date = LocalDate.parse(matcher.group("date"), DATE_FORMATTER);
            String description = matcher.group("description");
            BigDecimal amount = new BigDecimal(normalizeAmount(matcher.group("amount")));
            String currency = resolveCurrency(matcher.group("currency"));

            return new ParsedTransaction(date, description, amount, currency);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Некорректная дата операции: " + rawLine, exception);
        }
    }

    private String normalizeAmount(String amount) {
        return amount.replace(" ", "").replace(',', '.');
    }

    private String resolveCurrency(String currency) {
        return currency == null ? DEFAULT_CURRENCY : currency.toUpperCase(Locale.ROOT);
    }
}
