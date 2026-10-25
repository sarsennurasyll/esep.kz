package com.esep.statementimport.model;

import java.time.LocalDate;

/**
 * Результат прикладного сценария импорта банковской выписки.
 */
public record StatementImportResult(
        Long statementId,
        int operationsTotal,
        int recognizedOperations,
        int unknownOperations,
        int withoutMerchantOperations,
        LocalDate periodFrom,
        LocalDate periodTo
) {
}
