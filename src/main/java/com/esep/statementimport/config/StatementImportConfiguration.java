package com.esep.statementimport.config;

import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantrecognition.service.DefaultMerchantRecognitionService;
import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.interfaces.MerchantResolver;
import com.esep.merchantresolver.service.InMemoryMerchantResolver;
import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.normalization.service.DefaultMerchantNormalizer;
import com.esep.persistence.interfaces.StatementCatalog;
import com.esep.persistence.interfaces.TransactionCatalog;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.kaspi.KaspiStatementParser;
import com.esep.statementimport.pdf.PdfBoxTextExtractor;
import com.esep.statementimport.pdf.PdfTextExtractor;
import com.esep.statementimport.service.DefaultStatementImportUseCase;
import com.esep.statementimport.service.DefaultStatementImporter;
import com.esep.statementimport.service.StatementPeriodResolver;
import com.esep.statementimport.service.TransactionFingerprintGenerator;
import com.esep.statementimport.service.TransactionImportProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Собирает компоненты текущего сценария импорта Kaspi-выписок.
 */
@Configuration
public class StatementImportConfiguration {

    @Bean
    PdfTextExtractor pdfTextExtractor() {
        return new PdfBoxTextExtractor();
    }

    @Bean
    StatementParser statementParser(PdfTextExtractor pdfTextExtractor) {
        return new KaspiStatementParser(pdfTextExtractor);
    }

    @Bean
    MerchantNormalizer merchantNormalizer() {
        return new DefaultMerchantNormalizer();
    }

    @Bean
    MerchantResolver merchantResolver(
            MerchantCatalog merchantCatalog,
            MerchantAliasCatalog merchantAliasCatalog
    ) {
        return new InMemoryMerchantResolver(merchantCatalog, merchantAliasCatalog);
    }

    @Bean
    MerchantRecognitionService merchantRecognitionService(
            MerchantNormalizer merchantNormalizer,
            MerchantResolver merchantResolver
    ) {
        return new DefaultMerchantRecognitionService(merchantNormalizer, merchantResolver);
    }

    @Bean
    TransactionImportProcessor transactionImportProcessor(MerchantRecognitionService merchantRecognitionService) {
        return new TransactionImportProcessor(merchantRecognitionService);
    }

    @Bean
    DefaultStatementImporter statementImporter(MerchantRecognitionService merchantRecognitionService) {
        return new DefaultStatementImporter(merchantRecognitionService);
    }

    @Bean
    TransactionFingerprintGenerator transactionFingerprintGenerator() {
        return new TransactionFingerprintGenerator();
    }

    @Bean
    StatementPeriodResolver statementPeriodResolver() {
        return new StatementPeriodResolver();
    }

    @Bean
    DefaultStatementImportUseCase statementImportUseCase(
            StatementParser statementParser,
            DefaultStatementImporter statementImporter,
            TransactionImportProcessor transactionImportProcessor,
            TransactionFingerprintGenerator transactionFingerprintGenerator,
            StatementPeriodResolver statementPeriodResolver,
            StatementCatalog statementCatalog,
            TransactionCatalog transactionCatalog
    ) {
        return new DefaultStatementImportUseCase(
                statementParser,
                statementImporter,
                transactionImportProcessor,
                transactionFingerprintGenerator,
                statementPeriodResolver,
                statementCatalog,
                transactionCatalog
        );
    }
}
