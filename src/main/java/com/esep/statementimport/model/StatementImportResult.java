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
        int skippedDuplicateOperations,
        int operationsMissingFromStatement,
        LocalDate periodFrom,
        LocalDate periodTo
) {
}
