package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Находит строки операций, период и итоги шапки в текстовом представлении выписки Kaspi.
 */
class KaspiTransactionExtractor {

    private static final Pattern OPERATION_LINE = Pattern.compile("^\\d{2}\\.\\d{2}\\.(?:\\d{2}|\\d{4})\\s+.+$");
    private static final List<String> OPERATION_SECTION_MARKERS = List.of("ОПЕРАЦ", "TRANSACTION");
    private static final Pattern STATEMENT_PERIOD = Pattern.compile(
            "за период с (?<from>\\d{2}\\.\\d{2}\\.\\d{2}) по (?<to>\\d{2}\\.\\d{2}\\.\\d{2})"
    );
    private static final Pattern SUMMARY_TOTAL = Pattern.compile(
            "^(?<type>" + KaspiOperationTypes.summaryNamesPattern() + ") (?<sign>[+-]) ?"
                    + "(?<amount>(?:\\d{1,3}(?: \\d{3})*|\\d+),\\d{2}) ₸(?:\\s.*)?$"
    );
    // Из шапки берутся только последние четыре цифры маски карты; имя владельца и номер счёта не читаются.
    private static final Pattern CARD_NUMBER_SUFFIX = Pattern.compile("Номер карты: \\*(?<suffix>\\d{4})\\b");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd.MM.uu");

    RawStatement extract(String statementText) {
        List<String> transactionLines = new ArrayList<>();
        Map<BankOperationType, BigDecimal> summaryTotals = new EnumMap<>(BankOperationType.class);
        LocalDate periodFrom = null;
        LocalDate periodTo = null;
        String cardNumberSuffix = null;
        boolean operationsSectionStarted = false;

        for (String line : statementText.lines().toList()) {
            String normalizedLine = line.strip().replaceAll("\\s+", " ");

            if (periodFrom == null) {
                Matcher period = STATEMENT_PERIOD.matcher(normalizedLine);
                if (period.find()) {
                    periodFrom = LocalDate.parse(period.group("from"), SHORT_DATE);
                    periodTo = LocalDate.parse(period.group("to"), SHORT_DATE);
                }
            }

            if (cardNumberSuffix == null && transactionLines.isEmpty()) {
                Matcher card = CARD_NUMBER_SUFFIX.matcher(normalizedLine);
                if (card.find()) {
                    cardNumberSuffix = card.group("suffix");
                }
            }

            // Итоги шапки читаются только до первой операции, чтобы детали операций не приняли за итог.
            if (transactionLines.isEmpty()) {
                readSummaryTotal(normalizedLine, summaryTotals);
            }

            if (!operationsSectionStarted && isOperationSectionStart(normalizedLine)) {
                operationsSectionStarted = true;
                continue;
            }

            if (operationsSectionStarted && OPERATION_LINE.matcher(normalizedLine).matches()) {
                transactionLines.add(normalizedLine);
            }
        }

        return new RawStatement(transactionLines, cardNumberSuffix, periodFrom, periodTo, summaryTotals);
    }

    private void readSummaryTotal(String line, Map<BankOperationType, BigDecimal> summaryTotals) {
        Matcher total = SUMMARY_TOTAL.matcher(line);
        if (!total.matches()) {
            return;
        }

        BigDecimal amount = new BigDecimal(total.group("amount").replace(" ", "").replace(',', '.'));
        KaspiOperationTypes.fromSummaryName(total.group("type")).ifPresent(type ->
                summaryTotals.putIfAbsent(type, "-".equals(total.group("sign")) ? amount.negate() : amount)
        );
    }

    private boolean isOperationSectionStart(String line) {
        String upperCaseLine = line.toUpperCase(Locale.ROOT);
        return OPERATION_SECTION_MARKERS.stream().anyMatch(upperCaseLine::contains);
    }
}
