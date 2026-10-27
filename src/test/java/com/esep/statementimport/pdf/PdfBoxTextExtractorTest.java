package com.esep.statementimport.pdf;

import com.esep.statementimport.model.ImportErrorCode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    @Test
    void shouldRejectFileWithoutPdfSignatureBeforeParsing() {
        byte[] fakePdf = "MZ\u0090\u0000 not a pdf, renamed to statement.pdf".getBytes(StandardCharsets.ISO_8859_1);

        assertThatThrownBy(() -> pdfTextExtractor.extract(new ByteArrayInputStream(fakePdf)))
                .isInstanceOf(PdfExtractionException.class)
                .hasMessageContaining("не является PDF")
                .extracting(exception -> ((PdfExtractionException) exception).code()).isEqualTo(ImportErrorCode.PDF_NOT_PDF);
    }

    @Test
    void shouldRejectCorruptedPdfWithValidSignature() {
        byte[] corrupted = "%PDF-1.7\n1 0 obj << /Type /Catalog /Pages 2 0 R >> broken".getBytes(StandardCharsets.US_ASCII);

        assertThatThrownBy(() -> pdfTextExtractor.extract(new ByteArrayInputStream(corrupted)))
                .isInstanceOf(PdfExtractionException.class)
                .hasMessageContaining("повреждён")
                .extracting(exception -> ((PdfExtractionException) exception).code()).isEqualTo(ImportErrorCode.PDF_CORRUPTED);
    }

    @Test
    void shouldRejectPasswordProtectedPdf() throws IOException {
        byte[] encrypted;
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            StandardProtectionPolicy policy = new StandardProtectionPolicy("owner-secret", "user-secret", new AccessPermission());
            policy.setEncryptionKeyLength(128);
            document.protect(policy);
            document.save(output);
            encrypted = output.toByteArray();
        }

        assertThatThrownBy(() -> pdfTextExtractor.extract(new ByteArrayInputStream(encrypted)))
                .isInstanceOf(PdfExtractionException.class)
                .hasMessageContaining("паролем")
                .extracting(exception -> ((PdfExtractionException) exception).code()).isEqualTo(ImportErrorCode.PDF_ENCRYPTED);
    }

    @Test
    void shouldAcceptSignatureAfterLeadingBytesWithinHeaderWindow() throws IOException {
        byte[] pdf = blankPdf(1);
        byte[] withPrefix = new byte[pdf.length + 16];
        System.arraycopy(pdf, 0, withPrefix, 16, pdf.length);

        assertThat(new PdfBoxTextExtractor().extract(new ByteArrayInputStream(withPrefix))).isNotNull();
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
