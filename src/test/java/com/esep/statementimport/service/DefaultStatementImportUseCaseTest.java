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
import com.esep.statementimport.interfaces.StatementParserRegistry;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;
import com.esep.statementimport.model.StatementImportResult;
import com.esep.entity.BankOperationType;
import com.esep.persistence.model.ExistingOperationKey;
import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.PurposeDecision;
import com.esep.moneyflow.service.MoneyFlowClassifierRegistry;
import com.esep.statementimport.kaspi.KaspiMoneyFlowClassifier;
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

        StatementImportResult result = useCase.importStatement(
                new ByteArrayInputStream("source".getBytes()),
                BankType.KASPI,
                "statement.pdf"
        );

        assertThat(result.statementId()).isEqualTo(1L);
        assertThat(result.operationsTotal()).isEqualTo(2);
        assertThat(result.recognizedOperations()).isEqualTo(1);
        assertThat(result.unknownOperations()).isEqualTo(1);
        assertThat(statementCatalog.savedStatement.maskedAccountNumber()).isEqualTo("****5678");
        assertThat(statementCatalog.savedStatement.periodFrom()).isEqualTo(LocalDate.of(2026, 7, 12));
        assertThat(statementCatalog.savedStatement.periodTo()).isEqualTo(LocalDate.of(2026, 7, 13));
        assertThat(transactionCatalog.savedTransactions).hasSize(2);
        assertThat(transactionCatalog.savedTransactions.getFirst().merchantReference().value()).isEqualTo("1");
        assertThat(transactionCatalog.savedTransactions.get(1).merchantReference()).isNull();
        assertThat(transactionCatalog.savedTransactions)
                .extracting(command -> command.moneyFlow().source())
                .containsOnly(ClassificationSource.BANK_RULE);
        verify(recognitionService, times(1)).recognize("MAGNUM");
        verify(recognitionService, times(1)).recognize("UNKNOWN SHOP");
    }

    @Test
    void shouldSkipOperationsAlreadyImportedFromOverlappingStatementOfSameAccount() {
        StatementParser parser = mock(StatementParser.class);
        MerchantRecognitionService recognitionService = mock(MerchantRecognitionService.class);
        RecordingStatementCatalog statementCatalog = new RecordingStatementCatalog(false);
        RecordingTransactionCatalog transactionCatalog = new RecordingTransactionCatalog();
        transactionCatalog.existingOperations = List.of(new ExistingOperationKey(
                LocalDate.of(2026, 7, 12), new BigDecimal("-14500.00"), BankOperationType.PURCHASE, "MAGNUM"
        ));
        when(parser.parse(any())).thenReturn(new ParsedStatement("1234", null, null, List.of(
                new ParsedTransaction(LocalDate.of(2026, 7, 12), "MAGNUM", new BigDecimal("-14500"), "KZT", BankOperationType.PURCHASE, 0),
                new ParsedTransaction(LocalDate.of(2026, 7, 13), "UNKNOWN SHOP", new BigDecimal("-2100"), "KZT", BankOperationType.PURCHASE, 1)
        )));
        when(recognitionService.recognize("UNKNOWN SHOP")).thenReturn(notRecognized("UNKNOWN SHOP"));

        StatementImportResult result = useCase(parser, recognitionService, statementCatalog, transactionCatalog)
                .importStatement(new ByteArrayInputStream("overlap".getBytes()), BankType.KASPI, "statement.pdf");

        assertThat(transactionCatalog.requestedAccount).isEqualTo("****1234");
        assertThat(result.skippedDuplicateOperations()).isEqualTo(1);
        assertThat(result.operationsTotal()).isEqualTo(1);
        assertThat(transactionCatalog.savedTransactions).extracting(TransactionPersistenceCommand::description)
                .containsExactly("UNKNOWN SHOP");
        verify(recognitionService, never()).recognize("MAGNUM");
    }

    @Test
    void shouldReportPreviouslyImportedOperationsMissingFromNewStatement() {
        StatementParser parser = mock(StatementParser.class);
        MerchantRecognitionService recognitionService = mock(MerchantRecognitionService.class);
        RecordingTransactionCatalog transactionCatalog = new RecordingTransactionCatalog();
        transactionCatalog.existingOperations = List.of(
                new ExistingOperationKey(LocalDate.of(2026, 7, 13), new BigDecimal("-900"), BankOperationType.PURCHASE, "CANCELLED SHOP"),
                new ExistingOperationKey(LocalDate.of(2026, 7, 14), new BigDecimal("-300"), BankOperationType.PURCHASE, "LATE SHOP")
        );
        when(parser.parse(any())).thenReturn(new ParsedStatement("1234", null, null, List.of(
                new ParsedTransaction(LocalDate.of(2026, 7, 12), "UNKNOWN SHOP", new BigDecimal("-100"), "KZT", BankOperationType.PURCHASE, 0),
                new ParsedTransaction(LocalDate.of(2026, 7, 14), "UNKNOWN SHOP", new BigDecimal("-200"), "KZT", BankOperationType.PURCHASE, 1)
        )));
        when(recognitionService.recognize("UNKNOWN SHOP")).thenReturn(notRecognized("UNKNOWN SHOP"));

        StatementImportResult result = useCase(parser, recognitionService, new RecordingStatementCatalog(false), transactionCatalog)
                .importStatement(new ByteArrayInputStream("missing".getBytes()), BankType.KASPI, "statement.pdf");

        // Операция последнего дня периода не считается пропавшей: выписка могла быть выгружена до её проведения.
        assertThat(result.operationsMissingFromStatement()).isEqualTo(1);
        assertThat(transactionCatalog.savedTransactions).hasSize(2);
    }

    @Test
    void shouldNotCompareWithPreviousStatementsWhenAccountIsUnknown() {
        StatementParser parser = mock(StatementParser.class);
        MerchantRecognitionService recognitionService = mock(MerchantRecognitionService.class);
        RecordingTransactionCatalog transactionCatalog = new RecordingTransactionCatalog();
        when(parser.parse(any())).thenReturn(new ParsedStatement(null, null, null, List.of(
                new ParsedTransaction(LocalDate.of(2026, 7, 12), "UNKNOWN SHOP", new BigDecimal("-100"), "KZT", BankOperationType.PURCHASE, 0)
        )));
        when(recognitionService.recognize("UNKNOWN SHOP")).thenReturn(notRecognized("UNKNOWN SHOP"));

        StatementImportResult result = useCase(parser, recognitionService, new RecordingStatementCatalog(false), transactionCatalog)
                .importStatement(new ByteArrayInputStream("no-account".getBytes()), BankType.KASPI, "statement.pdf");

        assertThat(transactionCatalog.requestedAccount).isNull();
        assertThat(result.skippedDuplicateOperations()).isZero();
        assertThat(transactionCatalog.savedTransactions).hasSize(1);
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
        StatementParserRegistry statementParserRegistry = mock(StatementParserRegistry.class);
        when(statementParserRegistry.getParser(BankType.KASPI)).thenReturn(parser);
        return new DefaultStatementImportUseCase(
                statementParserRegistry,
                new DefaultStatementImporter(processor),
                new TransactionFingerprintGenerator(),
                new StatementPeriodResolver(),
                new MoneyFlowClassifierRegistry(List.of(new KaspiMoneyFlowClassifier())),
                (description, amount, flowType) -> PurposeDecision.unknown(),
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
        public Long save(StatementPersistenceCommand statement) {
            savedStatement = statement;
            return 1L;
        }
    }

    private static final class RecordingTransactionCatalog implements TransactionCatalog {

        private List<TransactionPersistenceCommand> savedTransactions;
        private List<ExistingOperationKey> existingOperations = List.of();
        private String requestedAccount;

        @Override
        public void saveAll(String statementSourceFileHash, List<TransactionPersistenceCommand> transactions) {
            savedTransactions = transactions;
        }

        @Override
        public List<ExistingOperationKey> findOperationKeys(BankType bankType, String maskedAccountNumber, LocalDate periodFrom, LocalDate periodTo) {
            requestedAccount = maskedAccountNumber;
            return existingOperations;
        }
    }
}
