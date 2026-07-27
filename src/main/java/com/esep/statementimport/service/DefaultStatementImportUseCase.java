package com.esep.statementimport.service;

import com.esep.entity.BankType;
import com.esep.entity.TransactionType;
import com.esep.persistence.interfaces.StatementCatalog;
import com.esep.persistence.interfaces.TransactionCatalog;
import com.esep.persistence.model.StatementPersistenceCommand;
import com.esep.persistence.model.TransactionPersistenceCommand;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.model.ImportResult;
import com.esep.statementimport.model.ParsedStatement;
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

    private final StatementParser statementParser;
    private final DefaultStatementImporter statementImporter;
    private final TransactionImportProcessor transactionImportProcessor;
    private final TransactionFingerprintGenerator transactionFingerprintGenerator;
    private final StatementPeriodResolver statementPeriodResolver;
    private final StatementCatalog statementCatalog;
    private final TransactionCatalog transactionCatalog;

    public DefaultStatementImportUseCase(
            StatementParser statementParser,
            DefaultStatementImporter statementImporter,
            TransactionImportProcessor transactionImportProcessor,
            TransactionFingerprintGenerator transactionFingerprintGenerator,
            StatementPeriodResolver statementPeriodResolver,
            StatementCatalog statementCatalog,
            TransactionCatalog transactionCatalog
    ) {
        this.statementParser = statementParser;
        this.statementImporter = statementImporter;
        this.transactionImportProcessor = transactionImportProcessor;
        this.transactionFingerprintGenerator = transactionFingerprintGenerator;
        this.statementPeriodResolver = statementPeriodResolver;
        this.statementCatalog = statementCatalog;
        this.transactionCatalog = transactionCatalog;
    }

    @Transactional
    public ImportResult importStatement(InputStream input, BankType bankType, String originalFileName) {
        byte[] sourceFile = readSourceFile(input);
        String sourceFileHash = calculateSourceFileHash(sourceFile);

        if (statementCatalog.existsBySourceFileHash(sourceFileHash)) {
            throw new StatementAlreadyImportedException(sourceFileHash);
        }

        ParsedStatement parsedStatement = statementParser.parse(new ByteArrayInputStream(sourceFile));
        StatementPeriod statementPeriod = statementPeriodResolver.resolve(parsedStatement);
        List<ProcessedTransaction> processedTransactions = parsedStatement.transactions().stream()
                .map(transactionImportProcessor::process)
                .toList();

        statementCatalog.save(toStatementCommand(
                parsedStatement,
                statementPeriod,
                bankType,
                originalFileName,
                sourceFileHash
        ));
        transactionCatalog.saveAll(
                sourceFileHash,
                processedTransactions.stream()
                        .map(transaction -> toTransactionCommand(transaction, sourceFileHash))
                        .toList()
        );

        return statementImporter.createImportResult(processedTransactions);
    }

    private StatementPersistenceCommand toStatementCommand(
            ParsedStatement statement,
            StatementPeriod statementPeriod,
            BankType bankType,
            String originalFileName,
            String sourceFileHash
    ) {
        return new StatementPersistenceCommand(
                bankType,
                originalFileName,
                maskAccountNumber(statement.accountNumber()),
                sourceFileHash,
                statementPeriod.periodFrom(),
                statementPeriod.periodTo()
        );
    }

    private TransactionPersistenceCommand toTransactionCommand(
            ProcessedTransaction processedTransaction,
            String sourceFileHash
    ) {
        var transaction = processedTransaction.transaction();
        var recognition = processedTransaction.merchantRecognition();

        return new TransactionPersistenceCommand(
                transaction.date(),
                transaction.description(),
                transaction.amount(),
                transaction.currency(),
                TransactionType.UNKNOWN,
                recognition.merchantMatch().merchantReference(),
                transactionFingerprintGenerator.generate(
                        sourceFileHash,
                        transaction,
                        recognition.normalizedMerchant()
                )
        );
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
