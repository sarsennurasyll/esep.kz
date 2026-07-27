package com.esep.statementimport.service;

import com.esep.entity.BankType;
import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantrecognition.model.MerchantRecognitionResult;
import com.esep.merchantresolver.model.MerchantMatch;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.persistence.interfaces.StatementCatalog;
import com.esep.persistence.interfaces.TransactionCatalog;
import com.esep.persistence.model.StatementPersistenceCommand;
import com.esep.persistence.model.TransactionPersistenceCommand;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.model.ImportResult;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultStatementImportUseCaseTest {

    @Test
    void shouldImportStatementWithOneRecognitionPerTransaction() {
        StatementParser parser = mock(StatementParser.class);
        MerchantRecognitionService recognitionService = mock(MerchantRecognitionService.class);
        RecordingStatementCatalog statementCatalog = new RecordingStatementCatalog(false);
        RecordingTransactionCatalog transactionCatalog = new RecordingTransactionCatalog();
        ParsedStatement parsedStatement = new ParsedStatement(
                "KZ12345678",
                null,
                null,
                List.of(
                        transaction(LocalDate.of(2026, 7, 12), "MAGNUM", "14500.00"),
                        transaction(LocalDate.of(2026, 7, 13), "UNKNOWN SHOP", "2100.00")
                )
        );
        when(parser.parse(any())).thenReturn(parsedStatement);
        when(recognitionService.recognize("MAGNUM")).thenReturn(recognized("MAGNUM", "MAGNUM", "1"));
        when(recognitionService.recognize("UNKNOWN SHOP")).thenReturn(notRecognized("UNKNOWN SHOP"));

        DefaultStatementImportUseCase useCase = useCase(
                parser,
                recognitionService,
                statementCatalog,
                transactionCatalog
        );

        ImportResult result = useCase.importStatement(
                new ByteArrayInputStream("source".getBytes()),
                BankType.KASPI,
                "statement.pdf"
        );

        assertThat(result.totalTransactions()).isEqualTo(2);
        assertThat(result.recognizedMerchants()).isEqualTo(1);
        assertThat(result.unknownMerchants()).isEqualTo(1);
        assertThat(statementCatalog.savedStatement.maskedAccountNumber()).isEqualTo("****5678");
        assertThat(statementCatalog.savedStatement.periodFrom()).isEqualTo(LocalDate.of(2026, 7, 12));
        assertThat(statementCatalog.savedStatement.periodTo()).isEqualTo(LocalDate.of(2026, 7, 13));
        assertThat(transactionCatalog.savedTransactions).hasSize(2);
        assertThat(transactionCatalog.savedTransactions.getFirst().merchantReference().value()).isEqualTo("1");
        assertThat(transactionCatalog.savedTransactions.get(1).merchantReference()).isNull();
        verify(recognitionService, times(1)).recognize("MAGNUM");
        verify(recognitionService, times(1)).recognize("UNKNOWN SHOP");
    }

    @Test
    void shouldRejectAlreadyImportedStatementBeforeParsing() {
        StatementParser parser = mock(StatementParser.class);
        MerchantRecognitionService recognitionService = mock(MerchantRecognitionService.class);
        DefaultStatementImportUseCase useCase = useCase(
                parser,
                recognitionService,
                new RecordingStatementCatalog(true),
                new RecordingTransactionCatalog()
        );

        assertThatThrownBy(() -> useCase.importStatement(
                new ByteArrayInputStream("source".getBytes()),
                BankType.KASPI,
                "statement.pdf"
        )).isInstanceOf(StatementAlreadyImportedException.class);

        verify(parser, never()).parse(any());
        verify(recognitionService, never()).recognize(any());
    }

    private DefaultStatementImportUseCase useCase(
            StatementParser parser,
            MerchantRecognitionService recognitionService,
            StatementCatalog statementCatalog,
            TransactionCatalog transactionCatalog
    ) {
        TransactionImportProcessor processor = new TransactionImportProcessor(recognitionService);
        return new DefaultStatementImportUseCase(
                parser,
                new DefaultStatementImporter(processor),
                processor,
                new TransactionFingerprintGenerator(),
                new StatementPeriodResolver(),
                statementCatalog,
                transactionCatalog
        );
    }

    private MerchantRecognitionResult recognized(String raw, String normalized, String reference) {
        return new MerchantRecognitionResult(
                raw,
                normalized,
                MerchantMatch.matched(new MerchantReference(reference), normalized, 1.0, true)
        );
    }

    private MerchantRecognitionResult notRecognized(String raw) {
        return new MerchantRecognitionResult(raw, raw, MerchantMatch.notMatched());
    }

    private ParsedTransaction transaction(LocalDate date, String description, String amount) {
        return new ParsedTransaction(date, description, new BigDecimal(amount), "KZT");
    }

    private static final class RecordingStatementCatalog implements StatementCatalog {

        private final boolean alreadyImported;
        private StatementPersistenceCommand savedStatement;

        private RecordingStatementCatalog(boolean alreadyImported) {
            this.alreadyImported = alreadyImported;
        }

        @Override
        public boolean existsBySourceFileHash(String sourceFileHash) {
            return alreadyImported;
        }

        @Override
        public void save(StatementPersistenceCommand statement) {
            savedStatement = statement;
        }
    }

    private static final class RecordingTransactionCatalog implements TransactionCatalog {

        private List<TransactionPersistenceCommand> savedTransactions;

        @Override
        public void saveAll(String statementSourceFileHash, List<TransactionPersistenceCommand> transactions) {
            savedTransactions = transactions;
        }
    }
}
