package com.esep.statementimport.service;

import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.statementimport.interfaces.StatementImporter;
import com.esep.statementimport.model.ImportResult;
import com.esep.statementimport.model.ParsedStatement;

import java.util.List;
import java.util.Objects;

/**
 * Обрабатывает операции распарсенной выписки через сервис распознавания продавцов.
 */
public class DefaultStatementImporter implements StatementImporter {

    private final TransactionImportProcessor transactionImportProcessor;

    public DefaultStatementImporter(MerchantRecognitionService merchantRecognitionService) {
        this(new TransactionImportProcessor(merchantRecognitionService));
    }

    DefaultStatementImporter(TransactionImportProcessor transactionImportProcessor) {
        this.transactionImportProcessor = transactionImportProcessor;
    }

    @Override
    public ImportResult importStatement(ParsedStatement statement) {
        Objects.requireNonNull(statement, "Statement must not be null");

        List<ProcessedTransaction> processedTransactions = statement.transactions().stream()
                .map(transactionImportProcessor::process)
                .toList();

        return createImportResult(processedTransactions);
    }

    ImportResult createImportResult(List<ProcessedTransaction> processedTransactions) {
        List<ProcessedTransaction> merchantCandidates = processedTransactions.stream()
                .filter(transaction -> transaction.transaction().bankOperationType().merchantCounterpartyPossible())
                .toList();
        int recognizedMerchants = (int) merchantCandidates.stream()
                .filter(transaction -> transaction.merchantRecognition().merchantMatch().matched())
                .count();

        return new ImportResult(
                processedTransactions.size(),
                recognizedMerchants,
                merchantCandidates.size() - recognizedMerchants,
                processedTransactions.size() - merchantCandidates.size()
        );
    }
}
