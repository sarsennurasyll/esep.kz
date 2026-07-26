package com.esep.statementimport.kaspi;

import java.util.List;

/**
 * Внутренний результат поиска необработанных строк операций Kaspi.
 */
record RawStatement(List<String> transactionLines) {

    RawStatement {
        transactionLines = List.copyOf(transactionLines);
    }
}
