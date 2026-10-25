package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;
import com.esep.statementimport.exception.StatementFormatException;
import com.esep.statementimport.model.ParsedTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class KaspiTransactionParser {
    private static final Pattern LINE = Pattern.compile("^(?<date>\\d{2}\\.\\d{2}\\.\\d{2})\\s+(?<amount>[+-]?\\s?(?:\\d{1,3}(?:\\s\\d{3})*|\\d+)(?:[.,]\\d+)?)\\s+(?:[^\\p{L}\\p{N}\\s]+\\s+)?(?<operation>\\p{L}+)\\s+(?<description>.+)$");
    private static final Pattern FULL_LINE = Pattern.compile("^(?<date>\\d{2}\\.\\d{2}\\.\\d{4})\\s+(?<description>.+?)\\s+(?<amount>[+-]?\\s?(?:\\d{1,3}(?:\\s\\d{3})*|\\d+)(?:[.,]\\d+)?)(?:\\s+(?<currency>[A-Za-z]{3}))?$");

    ParsedTransaction parse(String rawLine, int position) {
        if (rawLine == null) throw new IllegalArgumentException("Operation line must not be null");
        Matcher kaspi = LINE.matcher(rawLine.strip());
        if (kaspi.matches()) return createKaspiTransaction(kaspi, position);
        Matcher full = FULL_LINE.matcher(rawLine.strip());
        if (full.matches()) return create(full, DateTimeFormatter.ofPattern("dd.MM.uuuu"), full.group("currency"), position, BankOperationType.UNKNOWN, full.group("description"));
        // Текст строки не включается в сообщение: он содержит дату, сумму и контрагента.
        throw new StatementFormatException("Не удалось разобрать строку операции с позицией " + position);
    }

    private ParsedTransaction createKaspiTransaction(Matcher matcher, int position) {
        String operation = matcher.group("operation");
        BankOperationType operationType = KaspiOperationTypes.fromOperationName(operation).orElse(BankOperationType.UNKNOWN);
        // Для неизвестного типа название сохраняется в описании, чтобы не потерять сведения банка.
        String description = operationType == BankOperationType.UNKNOWN
                ? operation + " " + matcher.group("description")
                : matcher.group("description");
        return create(matcher, DateTimeFormatter.ofPattern("dd.MM.uu"), "KZT", position, operationType, description);
    }

    private ParsedTransaction create(Matcher matcher, DateTimeFormatter formatter, String currency, int position, BankOperationType operationType, String description) {
        return new ParsedTransaction(LocalDate.parse(matcher.group("date"), formatter), description,
                new BigDecimal(matcher.group("amount").replace(" ", "").replace(',', '.')),
                currency == null ? "KZT" : currency.toUpperCase(Locale.ROOT), operationType, position);
    }
}
