package com.esep.statementimport.service;

import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.statementimport.model.ParsedTransaction;

/**
 * Однократно распознаёт продавца для операции импорта.
 */
public class TransactionImportProcessor {

    private final MerchantRecognitionService merchantRecognitionService;

    public TransactionImportProcessor(MerchantRecognitionService merchantRecognitionService) {
        this.merchantRecognitionService = merchantRecognitionService;
    }

    ProcessedTransaction process(ParsedTransaction transaction) {
        return new ProcessedTransaction(
                transaction,
                merchantRecognitionService.recognize(transaction.description())
        );
    }
}
