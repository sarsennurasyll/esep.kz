package com.esep.statementimport.service;

import com.esep.merchantrecognition.model.MerchantRecognitionResult;
import com.esep.statementimport.model.ParsedTransaction;

/**
 * Внутренний результат обработки одной операции импорта.
 */
record ProcessedTransaction(
        ParsedTransaction transaction,
        MerchantRecognitionResult merchantRecognition
) {
}
