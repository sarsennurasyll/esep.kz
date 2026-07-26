package com.esep.statementimport.integration;

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
import com.esep.statementimport.interfaces.StatementImporter;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.kaspi.KaspiStatementParser;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.pdf.PdfBoxTextExtractor;
import com.esep.statementimport.pdf.PdfTextExtractor;
import com.esep.statementimport.service.DefaultStatementImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KaspiImportPipelineIntegrationTest {

    private final MerchantRecognitionService recognitionService = createRecognitionService();
    private final StatementParser statementParser = createStatementParser();
    private final StatementImporter statementImporter = new DefaultStatementImporter(recognitionService);

    @Test
    @EnabledIfSystemProperty(named = "kaspi.pdf.path", matches = ".+")
    void shouldProcessKaspiPdfThroughFullImportPipeline() throws IOException {
        Path statementPath = Path.of(System.getProperty("kaspi.pdf.path"));

        try (InputStream input = Files.newInputStream(statementPath)) {
            ParsedStatement statement = statementParser.parse(input);
            var importResult = statementImporter.importStatement(statement);

            long expectedRecognizedMerchants = statement.transactions().stream()
                    .filter(transaction -> recognitionService.recognize(transaction.description()).merchantMatch().matched())
                    .count();

            assertThat(statement.transactions()).isNotEmpty();
            assertThat(importResult.totalTransactions()).isEqualTo(statement.transactions().size());
            assertThat(importResult.recognizedMerchants()).isEqualTo(expectedRecognizedMerchants);
            assertThat(importResult.unknownMerchants())
                    .isEqualTo(importResult.totalTransactions() - importResult.recognizedMerchants());
            assertRecognizedIfPresent(statement, "MAGNUM");
            assertRecognizedIfPresent(statement, "YANDEX");
            assertRecognizedIfPresent(statement, "EUROPHARMA");
        }
    }

    private void assertRecognizedIfPresent(ParsedStatement statement, String marker) {
        statement.transactions().stream()
                .filter(transaction -> transaction.description().contains(marker))
                .findFirst()
                .ifPresent(transaction -> assertThat(
                        recognitionService.recognize(transaction.description()).merchantMatch().matched()
                ).isTrue());
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
}
