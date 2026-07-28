package com.esep.statementimport.kaspi;

import com.esep.statementimport.pdf.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class KaspiStatementParserTest {

    @Test
    void shouldCreateParsedTransactionsFromExtractedOperationLines() {
        PdfTextExtractor pdfTextExtractor = input -> """
                Выписка Kaspi
                Операции
                12.07.2026 MAGNUM CASH&CARRY -14500
                13.07.2026 YANDEX.GO -2100
                """;
        var parser = new KaspiStatementParser(pdfTextExtractor);

        var statement = parser.parse(new ByteArrayInputStream(new byte[0]));

        assertThat(statement.transactions()).hasSize(2);
        assertThat(statement.transactions().getFirst().description()).isEqualTo("MAGNUM CASH&CARRY");
        assertThat(statement.transactions().getFirst().amount()).isEqualByComparingTo(new BigDecimal("-14500"));
        assertThat(statement.transactions().getFirst().sourceRecordPosition()).isZero();
        assertThat(statement.transactions().get(1).description()).isEqualTo("YANDEX.GO");
        assertThat(statement.transactions().get(1).sourceRecordPosition()).isEqualTo(1);
    }
}
