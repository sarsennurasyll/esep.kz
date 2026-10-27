package com.esep.statementimport.pdf;

import com.esep.statementimport.model.ImportErrorCode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfProcessingLimitsTest {

    private static final long SECOND = 1_000_000_000L;

    @Test
    void shouldStopParsingWhenDeadlineIsExceededInsteadOfFinishingDocument() throws IOException {
        byte[] pdf = textPdf(5, "Synthetic page");
        AtomicInteger clockCalls = new AtomicInteger();
        // Каждое обращение к часам «сдвигает» время на секунду; лимит — полторы секунды.
        PdfBoxTextExtractor extractor = new PdfBoxTextExtractor(
                new PdfProcessingLimits(200, 2_000_000, Duration.ofMillis(1_500), 64L * 1024 * 1024),
                () -> clockCalls.getAndIncrement() * SECOND
        );

        assertThatThrownBy(() -> extractor.extract(new ByteArrayInputStream(pdf)))
                .isInstanceOf(PdfProcessingTimeoutException.class)
                .extracting(exception -> ((PdfExtractionException) exception).code()).isEqualTo(ImportErrorCode.PDF_TIMEOUT);
        // Старт, проверка после загрузки, проверка после первой страницы — остальные четыре страницы не разбирались.
        assertThat(clockCalls.get()).isEqualTo(3);
    }

    @Test
    void shouldRejectDocumentWithTooMuchText() throws IOException {
        byte[] pdf = textPdf(3, "Synthetic page with some text");
        PdfBoxTextExtractor extractor = new PdfBoxTextExtractor(
                new PdfProcessingLimits(200, 40, Duration.ofSeconds(20), 64L * 1024 * 1024)
        );

        assertThatThrownBy(() -> extractor.extract(new ByteArrayInputStream(pdf)))
                .isInstanceOf(PdfLimitExceededException.class)
                .extracting(exception -> ((PdfExtractionException) exception).code()).isEqualTo(ImportErrorCode.PDF_TOO_MUCH_TEXT);
    }

    @Test
    void shouldReportPageLimitAsLimitExceeded() throws IOException {
        byte[] pdf = textPdf(3, "Synthetic page");

        assertThatThrownBy(() -> new PdfBoxTextExtractor(2).extract(new ByteArrayInputStream(pdf)))
                .isInstanceOf(PdfLimitExceededException.class)
                .extracting(exception -> ((PdfExtractionException) exception).code()).isEqualTo(ImportErrorCode.PDF_TOO_MANY_PAGES);
    }

    @Test
    void shouldExtractTextOfAllPagesWithinLimits() throws IOException {
        byte[] pdf = textPdf(3, "Synthetic page");

        String text = new PdfBoxTextExtractor().extract(new ByteArrayInputStream(pdf));

        assertThat(text.split("Synthetic page", -1)).hasSize(4);
    }

    @Test
    void shouldRejectNonPositiveLimits() {
        assertThatThrownBy(() -> new PdfProcessingLimits(0, 1, Duration.ofSeconds(1), 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PdfProcessingLimits(1, 1, Duration.ZERO, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private byte[] textPdf(int pages, String text) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            for (int index = 0; index < pages; index++) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(font, 12);
                    content.newLineAtOffset(72, 700);
                    content.showText(text);
                    content.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
