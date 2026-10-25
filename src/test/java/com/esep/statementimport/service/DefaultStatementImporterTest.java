package com.esep.statementimport.service;

import com.esep.entity.BankOperationType;
import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantrecognition.service.DefaultMerchantRecognitionService;
import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.interfaces.MerchantResolver;
import com.esep.merchantresolver.model.MerchantRecord;
import com.esep.merchantresolver.model.MerchantReference;
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
import java.util.Optional;

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

    @Test
    void shouldNotCountTransfersBoundToSystemRecipientAsRecognizedMerchants() {
        MerchantRecord personTransfers = new MerchantRecord(new MerchantReference("99"), "ПЕРЕВОДЫ ЛЮДЯМ");
        MerchantCatalog catalog = new MerchantCatalog() {
            public Optional<MerchantRecord> findByCanonicalName(String name) {
                return "ПЕРЕВОДЫ ЛЮДЯМ".equals(name) ? Optional.of(personTransfers) : Optional.empty();
            }

            public Optional<MerchantRecord> findByReference(MerchantReference reference) {
                return Optional.empty();
            }
        };
        StatementImporter importer = new DefaultStatementImporter(
                new TransactionImportProcessor(createRecognitionService(), catalog)
        );
        ParsedStatement statement = new ParsedStatement(null, null, null, List.of(
                new ParsedTransaction(LocalDate.of(2026, 7, 1), "MAGNUM CASH&CARRY", new BigDecimal("-100"), "KZT", BankOperationType.PURCHASE, 0),
                new ParsedTransaction(LocalDate.of(2026, 7, 1), "Тестов Т.", new BigDecimal("-200"), "KZT", BankOperationType.TRANSFER, 1),
                new ParsedTransaction(LocalDate.of(2026, 7, 1), "В Kaspi Банкомате", new BigDecimal("300"), "KZT", BankOperationType.TOP_UP, 2),
                new ParsedTransaction(LocalDate.of(2026, 7, 1), "Банкомат TEST", new BigDecimal("-400"), "KZT", BankOperationType.CASH_WITHDRAWAL, 3)
        ));

        var result = importer.importStatement(statement);

        assertThat(result.totalTransactions()).isEqualTo(4);
        assertThat(result.recognizedMerchants()).isEqualTo(1);
        assertThat(result.unknownMerchants()).isZero();
        assertThat(result.withoutMerchantOperations()).isEqualTo(3);
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
