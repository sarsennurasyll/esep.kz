package com.esep.statementimport.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void shouldRejectDocumentWithTooManyPages() throws IOException {
        byte[] threePages = blankPdf(3);

        assertThatThrownBy(() -> new PdfBoxTextExtractor(2).extract(new ByteArrayInputStream(threePages)))
                .isInstanceOf(PdfExtractionException.class);
        assertThat(new PdfBoxTextExtractor(3).extract(new ByteArrayInputStream(threePages))).isNotNull();
    }

    @Test
    void shouldRejectFileThatIsNotPdf() {
        assertThatThrownBy(() -> pdfTextExtractor.extract(new ByteArrayInputStream("not a pdf".getBytes())))
                .isInstanceOf(PdfExtractionException.class);
    }

    private byte[] blankPdf(int pages) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (int page = 0; page < pages; page++) {
                document.addPage(new PDPage());
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
