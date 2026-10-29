package com.esep.statementimport.api.dto;

import com.esep.statementimport.model.StatementImportResult;

import java.time.LocalDate;

/**
 * HTTP-ответ после успешного импорта банковской выписки.
 */
public record StatementImportResponse(
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

    public static StatementImportResponse from(StatementImportResult result) {
        return new StatementImportResponse(
                result.statementId(),
                result.operationsTotal(),
                result.recognizedOperations(),
                result.unknownOperations(),
                result.withoutMerchantOperations(),
                result.skippedDuplicateOperations(),
                result.operationsMissingFromStatement(),
                result.periodFrom(),
                result.periodTo()
        );
    }
}
