package com.esep.statementimport.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PdfBoxTextExtractorTest {

    private final PdfTextExtractor pdfTextExtractor = new PdfBoxTextExtractor();

    @Test
    @EnabledIfSystemProperty(named = "kaspi.pdf.path", matches = ".+")
    void shouldExtractTextFromKaspiStatement() throws IOException {
        Path statementPath = Path.of(System.getProperty("kaspi.pdf.path"));

        try (InputStream input = Files.newInputStream(statementPath)) {
            String text = pdfTextExtractor.extract(input);

            assertThat(text).isNotBlank();
            assertThat(text).containsAnyOf("MAGNUM", "YANDEX", "EUROPHARMA");
        }
    }
}
