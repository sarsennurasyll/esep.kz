package com.esep.statementimport.integration;

import com.esep.entity.BankType;
import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantrecognition.service.DefaultMerchantRecognitionService;
import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.interfaces.MerchantResolver;
import com.esep.merchantresolver.repository.InMemoryMerchantAliasCatalog;
import com.esep.merchantresolver.repository.InMemoryMerchantRepository;
import com.esep.merchantresolver.service.InMemoryMerchantResolver;
import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.normalization.service.DefaultMerchantNormalizer;
import com.esep.persistence.interfaces.StatementCatalog;
import com.esep.persistence.interfaces.TransactionCatalog;
import com.esep.persistence.model.StatementPersistenceCommand;
import com.esep.persistence.model.TransactionPersistenceCommand;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.kaspi.KaspiStatementParser;
import com.esep.statementimport.pdf.PdfBoxTextExtractor;
import com.esep.statementimport.pdf.PdfTextExtractor;
import com.esep.statementimport.service.DefaultStatementImportUseCase;
import com.esep.statementimport.service.DefaultStatementImporter;
import com.esep.statementimport.service.StatementPeriodResolver;
import com.esep.statementimport.service.TransactionFingerprintGenerator;
import com.esep.statementimport.service.TransactionImportProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatementImportUseCaseIntegrationTest {

    @Test
    @EnabledIfSystemProperty(named = "kaspi.pdf.path", matches = ".+")
    void shouldImportRealKaspiPdfThroughApplicationUseCase() throws IOException {
        RecordingStatementCatalog statementCatalog = new RecordingStatementCatalog();
        RecordingTransactionCatalog transactionCatalog = new RecordingTransactionCatalog();
        DefaultStatementImportUseCase useCase = createUseCase(statementCatalog, transactionCatalog);
        Path statementPath = Path.of(System.getProperty("kaspi.pdf.path"));

        try (InputStream input = Files.newInputStream(statementPath)) {
            var result = useCase.importStatement(input, BankType.KASPI, statementPath.getFileName().toString());

            assertThat(result.operationsTotal()).isPositive();
            assertThat(transactionCatalog.transactions).hasSize(result.operationsTotal());
            assertThat(statementCatalog.statement.periodFrom()).isNotNull();
            assertThat(statementCatalog.statement.periodTo()).isNotNull();
            assertThat(result.recognizedOperations()).isPositive();
        }
    }

    private DefaultStatementImportUseCase createUseCase(
            StatementCatalog statementCatalog,
            TransactionCatalog transactionCatalog
    ) {
        MerchantRecognitionService recognitionService = createRecognitionService();
        StatementParser statementParser = createStatementParser();

        return new DefaultStatementImportUseCase(
                statementParser,
                new DefaultStatementImporter(recognitionService),
                new TransactionImportProcessor(recognitionService),
                new TransactionFingerprintGenerator(),
                new StatementPeriodResolver(),
                statementCatalog,
                transactionCatalog
        );
    }

    private StatementParser createStatementParser() {
        PdfTextExtractor pdfTextExtractor = new PdfBoxTextExtractor();
        return new KaspiStatementParser(pdfTextExtractor);
    }

    private MerchantRecognitionService createRecognitionService() {
        MerchantNormalizer merchantNormalizer = new DefaultMerchantNormalizer();
        MerchantCatalog merchantCatalog = new InMemoryMerchantRepository();
        MerchantAliasCatalog merchantAliasCatalog = new InMemoryMerchantAliasCatalog();
        MerchantResolver merchantResolver = new InMemoryMerchantResolver(merchantCatalog, merchantAliasCatalog);
        return new DefaultMerchantRecognitionService(merchantNormalizer, merchantResolver);
    }

    private static final class RecordingStatementCatalog implements StatementCatalog {

        private StatementPersistenceCommand statement;

        @Override
        public boolean existsBySourceFileHash(String sourceFileHash) {
            return false;
        }

        @Override
        public Long save(StatementPersistenceCommand statement) {
            this.statement = statement;
            return 1L;
        }
    }

    private static final class RecordingTransactionCatalog implements TransactionCatalog {

        private List<TransactionPersistenceCommand> transactions;

        @Override
        public void saveAll(String statementSourceFileHash, List<TransactionPersistenceCommand> transactions) {
            this.transactions = transactions;
        }
    }
}
