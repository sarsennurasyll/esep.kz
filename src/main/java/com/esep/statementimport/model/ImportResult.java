package com.esep.statementimport.model;

/**
 * Неизменяемая статистика обработки банковской выписки.
 */
public record ImportResult(
        int totalTransactions,
        int recognizedMerchants,
        int unknownMerchants
) {
}
