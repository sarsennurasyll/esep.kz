package com.esep.statementimport.service;

import com.esep.entity.BankType;
import com.esep.entity.TransactionType;
import com.esep.moneyflow.service.MoneyFlowClassifierRegistry;
import com.esep.persistence.interfaces.StatementCatalog;
import com.esep.persistence.interfaces.TransactionCatalog;
import com.esep.persistence.model.StatementPersistenceCommand;
import com.esep.persistence.model.TransactionPersistenceCommand;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.interfaces.StatementParserRegistry;
import com.esep.statementimport.model.ImportResult;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.StatementImportResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Оркестрирует импорт выписки от исходного файла до сохранения операций.
 */
public class DefaultStatementImportUseCase {

    private final StatementParserRegistry statementParserRegistry;
    private final DefaultStatementImporter statementImporter;
    private final TransactionImportProcessor transactionImportProcessor;
    private final TransactionFingerprintGenerator transactionFingerprintGenerator;
    private final StatementPeriodResolver statementPeriodResolver;
    private final MoneyFlowClassifierRegistry moneyFlowClassifierRegistry;
    private final StatementCatalog statementCatalog;
    private final TransactionCatalog transactionCatalog;
    private final OverlappingOperationFilter overlappingOperationFilter = new OverlappingOperationFilter();

    public DefaultStatementImportUseCase(
            StatementParserRegistry statementParserRegistry,
            DefaultStatementImporter statementImporter,
            TransactionImportProcessor transactionImportProcessor,
            TransactionFingerprintGenerator transactionFingerprintGenerator,
            StatementPeriodResolver statementPeriodResolver,
            MoneyFlowClassifierRegistry moneyFlowClassifierRegistry,
            StatementCatalog statementCatalog,
            TransactionCatalog transactionCatalog
    ) {
        this.statementParserRegistry = statementParserRegistry;
        this.statementImporter = statementImporter;
        this.transactionImportProcessor = transactionImportProcessor;
        this.transactionFingerprintGenerator = transactionFingerprintGenerator;
        this.statementPeriodResolver = statementPeriodResolver;
        this.moneyFlowClassifierRegistry = moneyFlowClassifierRegistry;
        this.statementCatalog = statementCatalog;
        this.transactionCatalog = transactionCatalog;
    }

    @Transactional
    public StatementImportResult importStatement(InputStream input, BankType bankType, String originalFileName) {
        byte[] sourceFile = readSourceFile(input);
        String sourceFileHash = calculateSourceFileHash(sourceFile);

        if (statementCatalog.existsBySourceFileHash(sourceFileHash)) {
            throw new StatementAlreadyImportedException(sourceFileHash);
        }

        ParsedStatement parsedStatement = statementParserRegistry.getParser(bankType)
                .parse(new ByteArrayInputStream(sourceFile));
        StatementPeriod statementPeriod = statementPeriodResolver.resolve(parsedStatement);
        String maskedAccountNumber = maskAccountNumber(parsedStatement.accountNumber());
        OverlappingOperationFilter.OverlapResult overlap = excludeAlreadyImportedOperations(
                parsedStatement,
                bankType,
                maskedAccountNumber,
                statementPeriod
        );
        List<ProcessedTransaction> processedTransactions = overlap.newTransactions().stream()
                .map(transactionImportProcessor::process)
                .toList();

        Long statementId = statementCatalog.save(toStatementCommand(
                maskedAccountNumber,
                statementPeriod,
                bankType,
                originalFileName,
                sourceFileHash
        ));
        transactionCatalog.saveAll(
                sourceFileHash,
                processedTransactions.stream()
                        .map(transaction -> toTransactionCommand(transaction, bankType, sourceFileHash))
                        .toList()
        );

        ImportResult importResult = statementImporter.createImportResult(processedTransactions);
        return new StatementImportResult(
                statementId,
                importResult.totalTransactions(),
                importResult.recognizedMerchants(),
                importResult.unknownMerchants(),
                importResult.withoutMerchantOperations(),
                overlap.skippedDuplicates(),
                statementPeriod.periodFrom(),
                statementPeriod.periodTo()
        );
    }

    // Без идентичности счёта выписки нельзя надёжно связать, поэтому сравнение с прежними выписками не выполняется.
    private OverlappingOperationFilter.OverlapResult excludeAlreadyImportedOperations(
            ParsedStatement parsedStatement,
            BankType bankType,
            String maskedAccountNumber,
            StatementPeriod statementPeriod
    ) {
        if (maskedAccountNumber == null) {
            return new OverlappingOperationFilter.OverlapResult(parsedStatement.transactions(), 0);
        }
        return overlappingOperationFilter.filter(
                parsedStatement.transactions(),
                transactionCatalog.findOperationKeys(
                        bankType,
                        maskedAccountNumber,
                        statementPeriod.periodFrom(),
                        statementPeriod.periodTo()
                )
        );
    }

    private StatementPersistenceCommand toStatementCommand(
            String maskedAccountNumber,
            StatementPeriod statementPeriod,
            BankType bankType,
            String originalFileName,
            String sourceFileHash
    ) {
        return new StatementPersistenceCommand(
                bankType,
                originalFileName,
                maskedAccountNumber,
                sourceFileHash,
                statementPeriod.periodFrom(),
                statementPeriod.periodTo()
        );
    }

    private TransactionPersistenceCommand toTransactionCommand(
            ProcessedTransaction processedTransaction,
            BankType bankType,
            String sourceFileHash
    ) {
        var transaction = processedTransaction.transaction();
        var recognition = processedTransaction.merchantRecognition();

        return new TransactionPersistenceCommand(
                transaction.date(),
                transaction.description(),
                transaction.amount(),
                transaction.currency(),
                financialTransactionType(transaction),
                transaction.bankOperationType(),
                moneyFlowClassifierRegistry.classify(
                        bankType,
                        transaction.bankOperationType(),
                        transaction.amount(),
                        transaction.description()
                ),
                recognition.merchantMatch().merchantReference(),
                transactionFingerprintGenerator.generate(
                        sourceFileHash,
                        transaction,
                        recognition.normalizedMerchant()
                )
        );
    }

    private TransactionType financialTransactionType(com.esep.statementimport.model.ParsedTransaction transaction) {
        if (transaction.bankOperationType() == com.esep.entity.BankOperationType.TRANSFER) {
            return TransactionType.TRANSFER;
        }
        return transaction.amount().signum() > 0 ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    private byte[] readSourceFile(InputStream input) {
        try {
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read statement source file", exception);
        }
    }

    private String calculateSourceFileHash(byte[] sourceFile) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(sourceFile);
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return null;
        }

        String value = accountNumber.trim();
        return "****" + value.substring(Math.max(0, value.length() - 4));
    }
}
