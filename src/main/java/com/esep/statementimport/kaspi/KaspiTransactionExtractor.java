package com.esep.statementimport.kaspi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Находит строки операций в текстовом представлении выписки Kaspi.
 */
class KaspiTransactionExtractor {

    private static final Pattern OPERATION_LINE = Pattern.compile("^\\d{2}\\.\\d{2}\\.\\d{4}\\s+.+$");
    private static final List<String> OPERATION_SECTION_MARKERS = List.of("ОПЕРАЦ", "TRANSACTION");

    RawStatement extract(String statementText) {
        List<String> transactionLines = new ArrayList<>();
        boolean operationsSectionStarted = false;

        for (String line : statementText.lines().toList()) {
            String normalizedLine = line.strip().replaceAll("\\s+", " ");

            if (!operationsSectionStarted && isOperationSectionStart(normalizedLine)) {
                operationsSectionStarted = true;
                continue;
            }

            if (operationsSectionStarted && OPERATION_LINE.matcher(normalizedLine).matches()) {
                transactionLines.add(normalizedLine);
            }
        }

        return new RawStatement(transactionLines);
    }

    private boolean isOperationSectionStart(String line) {
        String upperCaseLine = line.toUpperCase(Locale.ROOT);
        return OPERATION_SECTION_MARKERS.stream().anyMatch(upperCaseLine::contains);
    }
}
