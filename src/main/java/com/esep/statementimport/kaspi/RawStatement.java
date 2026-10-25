package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Внутренний результат поиска необработанных строк операций и сведений шапки Kaspi.
 * Период и итоги отсутствуют, если в тексте нет шапки выписки.
 */
record RawStatement(
        List<String> transactionLines,
        LocalDate periodFrom,
        LocalDate periodTo,
        Map<BankOperationType, BigDecimal> summaryTotals
) {

    RawStatement {
        transactionLines = List.copyOf(transactionLines);
        summaryTotals = Map.copyOf(summaryTotals);
    }
}
