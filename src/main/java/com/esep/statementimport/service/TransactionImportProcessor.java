package com.esep.statementimport.service;

import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantrecognition.model.MerchantRecognitionResult;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.model.MerchantMatch;
import com.esep.entity.BankOperationType;
import com.esep.statementimport.model.ParsedTransaction;

/**
 * Однократно распознаёт продавца для операции импорта.
 */
public class TransactionImportProcessor {

    private final MerchantRecognitionService merchantRecognitionService;
    private final MerchantCatalog merchantCatalog;

    public TransactionImportProcessor(MerchantRecognitionService merchantRecognitionService) {
        this(merchantRecognitionService, null);
    }

    public TransactionImportProcessor(MerchantRecognitionService merchantRecognitionService, MerchantCatalog merchantCatalog) {
        this.merchantRecognitionService = merchantRecognitionService;
        this.merchantCatalog = merchantCatalog;
    }

    ProcessedTransaction process(ParsedTransaction transaction) {
        if (transaction.bankOperationType() == BankOperationType.PURCHASE) return new ProcessedTransaction(transaction, merchantRecognitionService.recognize(transaction.description()));
        if (transaction.bankOperationType() == BankOperationType.TRANSFER && transaction.amount().signum() < 0 && merchantCatalog != null) {
            var merchant = merchantCatalog.findByCanonicalName("ПЕРЕВОДЫ ЛЮДЯМ").orElseThrow(() -> new IllegalStateException("System transfer merchant was not found"));
            return new ProcessedTransaction(transaction, new MerchantRecognitionResult(transaction.description(), "ПЕРЕВОДЫ ЛЮДЯМ", MerchantMatch.matched(merchant.merchantReference(), merchant.canonicalName(), 1.0, true)));
        }
        return new ProcessedTransaction(transaction, new MerchantRecognitionResult(transaction.description(), "", MerchantMatch.notMatched()));
    }
}
