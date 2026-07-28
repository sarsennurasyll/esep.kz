package com.esep.merchantmanagement.integration;

import com.esep.merchantmanagement.interfaces.MerchantReadQuery;
import com.esep.merchantmanagement.service.DefaultMerchantManagementService;
import com.esep.merchantrecognition.service.DefaultMerchantRecognitionService;
import com.esep.merchantresolver.repository.InMemoryMerchantAliasCatalog;
import com.esep.merchantresolver.repository.InMemoryMerchantRepository;
import com.esep.merchantresolver.service.InMemoryMerchantResolver;
import com.esep.normalization.service.DefaultMerchantNormalizer;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;
import com.esep.statementimport.service.DefaultStatementImporter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantAliasReuseIntegrationTest {

    @Test
    void shouldRecognizeAliasDuringNextImportAfterUserMatch() {
        InMemoryMerchantRepository merchantCatalog = new InMemoryMerchantRepository();
        InMemoryMerchantAliasCatalog aliasCatalog = new InMemoryMerchantAliasCatalog();
        DefaultMerchantNormalizer normalizer = new DefaultMerchantNormalizer();
        DefaultMerchantManagementService managementService = new DefaultMerchantManagementService(
                List::of,
                (MerchantReadQuery) List::of,
                merchantCatalog,
                aliasCatalog,
                aliasCatalog,
                normalizer
        );
        var merchantReference = merchantCatalog.findByCanonicalName("MAGNUM").orElseThrow().merchantReference();

        managementService.match("UNKNOWN MAGNUM BRANCH", merchantReference);

        DefaultMerchantRecognitionService recognitionService = new DefaultMerchantRecognitionService(
                normalizer,
                new InMemoryMerchantResolver(merchantCatalog, aliasCatalog)
        );
        DefaultStatementImporter importer = new DefaultStatementImporter(recognitionService);

        var importResult = importer.importStatement(new ParsedStatement(
                "****1234",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                List.of(new ParsedTransaction(
                        LocalDate.of(2026, 7, 1),
                        "Unknown Magnum Branch",
                        new BigDecimal("-1000.00"),
                        "KZT"
                ))
        ));

        assertThat(importResult.totalTransactions()).isEqualTo(1);
        assertThat(importResult.recognizedMerchants()).isEqualTo(1);
        assertThat(importResult.unknownMerchants()).isZero();
    }
}
