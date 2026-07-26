package com.esep.statementimport.service;

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
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultStatementImporterTest {

    private final StatementImporter statementImporter = new DefaultStatementImporter(createRecognitionService());

    @Test
    void shouldReturnRecognitionStatisticsForParsedStatement() {
        ParsedStatement statement = new ParsedStatement(
                "KZ123456789",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                List.of(
                        transaction("MAGNUM CASH&CARRY"),
                        transaction("YANDEX.GO"),
                        transaction("EUROPHARMA"),
                        transaction("НЕИЗВЕСТНЫЙ МАГАЗИН")
                )
        );

        var result = statementImporter.importStatement(statement);

        assertThat(result.totalTransactions()).isEqualTo(4);
        assertThat(result.recognizedMerchants()).isEqualTo(3);
        assertThat(result.unknownMerchants()).isEqualTo(1);
    }

    private ParsedTransaction transaction(String description) {
        return new ParsedTransaction(
                LocalDate.of(2026, 7, 1),
                description,
                BigDecimal.TEN,
                "KZT"
        );
    }

    private MerchantRecognitionService createRecognitionService() {
        MerchantNormalizer merchantNormalizer = new DefaultMerchantNormalizer();
        MerchantCatalog merchantCatalog = new InMemoryMerchantRepository();
        MerchantAliasCatalog merchantAliasCatalog = new InMemoryMerchantAliasCatalog();
        MerchantResolver merchantResolver = new InMemoryMerchantResolver(merchantCatalog, merchantAliasCatalog);
        return new DefaultMerchantRecognitionService(merchantNormalizer, merchantResolver);
    }
}
