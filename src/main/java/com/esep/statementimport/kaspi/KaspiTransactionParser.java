package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;
import com.esep.statementimport.model.ParsedTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class KaspiTransactionParser {
    private static final Pattern LINE = Pattern.compile("^(?<date>\\d{2}\\.\\d{2}\\.\\d{2})\\s+(?<amount>[+-]?\\s?(?:\\d{1,3}(?:\\s\\d{3})*|\\d+)(?:[.,]\\d+)?)\\s+(?:[^\\p{L}\\p{N}\\s]+\\s+)?(?<operation>Покупка|Пополнение|Перевод)\\s+(?<description>.+)$");
    private static final Pattern FULL_LINE = Pattern.compile("^(?<date>\\d{2}\\.\\d{2}\\.\\d{4})\\s+(?<description>.+?)\\s+(?<amount>[+-]?\\s?(?:\\d{1,3}(?:\\s\\d{3})*|\\d+)(?:[.,]\\d+)?)(?:\\s+(?<currency>[A-Za-z]{3}))?$");

    ParsedTransaction parse(String rawLine, int position) {
        if (rawLine == null) throw new IllegalArgumentException("Operation line must not be null");
        Matcher kaspi = LINE.matcher(rawLine.strip());
        if (kaspi.matches()) return create(kaspi, DateTimeFormatter.ofPattern("dd.MM.uu"), "KZT", position, operation(kaspi.group("operation")));
        Matcher full = FULL_LINE.matcher(rawLine.strip());
        if (full.matches()) return create(full, DateTimeFormatter.ofPattern("dd.MM.uuuu"), full.group("currency"), position, BankOperationType.UNKNOWN);
        throw new IllegalArgumentException("Invalid operation line: " + rawLine);
    }

    private ParsedTransaction create(Matcher matcher, DateTimeFormatter formatter, String currency, int position, BankOperationType operationType) {
        return new ParsedTransaction(LocalDate.parse(matcher.group("date"), formatter), matcher.group("description"),
                new BigDecimal(matcher.group("amount").replace(" ", "").replace(',', '.')),
                currency == null ? "KZT" : currency.toUpperCase(Locale.ROOT), operationType, position);
    }

    private BankOperationType operation(String value) {
        return switch (value) { case "Покупка" -> BankOperationType.PURCHASE; case "Перевод" -> BankOperationType.TRANSFER; case "Пополнение" -> BankOperationType.TOP_UP; default -> BankOperationType.UNKNOWN; };
    }
}
