package com.esep.statementimport.kaspi;

import com.esep.statementimport.pdf.PdfBoxTextExtractor;
import com.esep.statementimport.pdf.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KaspiTransactionExtractorTest {

    private final PdfTextExtractor pdfTextExtractor = new PdfBoxTextExtractor();
    private final KaspiTransactionExtractor transactionExtractor = new KaspiTransactionExtractor();

    @Test
    void shouldExtractOperationLinesFromKaspiText() {
        String statementText = """
                Выписка Kaspi
                Операции
                12.07.2026 MAGNUM CASH&CARRY -14500
                13.07.2026 YANDEX.GO -2100
                14.07.2026 EUROPHARMA -4500
                """;

        RawStatement rawStatement = transactionExtractor.extract(statementText);

        assertThat(rawStatement.transactionLines()).containsExactly(
                "12.07.2026 MAGNUM CASH&CARRY -14500",
                "13.07.2026 YANDEX.GO -2100",
                "14.07.2026 EUROPHARMA -4500"
        );
    }

    @Test
    @EnabledIfSystemProperty(named = "kaspi.pdf.path", matches = ".+")
    void shouldExtractKnownOperationsFromKaspiStatement() throws IOException {
        Path statementPath = Path.of(System.getProperty("kaspi.pdf.path"));

        try (InputStream input = Files.newInputStream(statementPath)) {
            RawStatement rawStatement = transactionExtractor.extract(pdfTextExtractor.extract(input));

            assertThat(rawStatement.transactionLines()).isNotEmpty();
            assertThat(rawStatement.transactionLines()).anyMatch(line -> line.contains("MAGNUM"));
            assertThat(rawStatement.transactionLines()).anyMatch(line -> line.contains("YANDEX"));
        }
    }
}
