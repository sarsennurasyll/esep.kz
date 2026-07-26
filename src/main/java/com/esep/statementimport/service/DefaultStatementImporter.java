package com.esep.statementimport.service;

import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.statementimport.interfaces.StatementImporter;
import com.esep.statementimport.model.ImportResult;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;

import java.util.Objects;

/**
 * Обрабатывает операции распарсенной выписки через сервис распознавания продавцов.
 */
public class DefaultStatementImporter implements StatementImporter {

    private final MerchantRecognitionService merchantRecognitionService;

    public DefaultStatementImporter(MerchantRecognitionService merchantRecognitionService) {
        this.merchantRecognitionService = merchantRecognitionService;
    }

    @Override
    public ImportResult importStatement(ParsedStatement statement) {
        Objects.requireNonNull(statement, "Statement must not be null");

        int recognizedMerchants = 0;
        int unknownMerchants = 0;

        for (ParsedTransaction transaction : statement.transactions()) {
            boolean matched = merchantRecognitionService.recognize(transaction.description())
                    .merchantMatch()
                    .matched();

            if (matched) {
                recognizedMerchants++;
            } else {
                unknownMerchants++;
            }
        }

        return new ImportResult(
                statement.transactions().size(),
                recognizedMerchants,
                unknownMerchants
        );
    }
}
