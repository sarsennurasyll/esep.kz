package com.esep.statementimport.service;

import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.statementimport.interfaces.StatementImporter;
import com.esep.statementimport.model.ImportResult;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;

import java.util.List;
import java.util.Objects;

/**
 * Обрабатывает операции распарсенной выписки через сервис распознавания продавцов.
 * Единственная точка обработки операций при импорте: сценарий импорта и статистика результата
 * используют один и тот же {@link TransactionImportProcessor}.
 */
public class DefaultStatementImporter implements StatementImporter {

    private final TransactionImportProcessor transactionImportProcessor;

    public DefaultStatementImporter(MerchantRecognitionService merchantRecognitionService) {
        this(new TransactionImportProcessor(merchantRecognitionService));
    }

    public DefaultStatementImporter(TransactionImportProcessor transactionImportProcessor) {
        this.transactionImportProcessor = transactionImportProcessor;
    }

    @Override
    public ImportResult importStatement(ParsedStatement statement) {
        Objects.requireNonNull(statement, "Statement must not be null");

        return createImportResult(process(statement.transactions()));
    }

    List<ProcessedTransaction> process(List<ParsedTransaction> transactions) {
        return transactions.stream()
                .map(transactionImportProcessor::process)
                .toList();
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
