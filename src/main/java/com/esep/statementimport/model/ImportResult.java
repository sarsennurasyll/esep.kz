package com.esep.statementimport.model;

/**
 * Неизменяемая статистика обработки банковской выписки.
 * Распознавание продавца применимо только к операциям, контрагентом которых может быть продавец;
 * переводы, пополнения, снятие наличных и комиссии учитываются отдельно и не считаются «распознанными».
 */
public record ImportResult(
        int totalTransactions,
        int recognizedMerchants,
        int unknownMerchants,
        int withoutMerchantOperations
) {
}
