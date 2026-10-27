package com.esep.statementimport.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Извлекает текст из PDF-документа с помощью Apache PDFBox.
 * Файл без сигнатуры PDF, повреждённый или защищённый паролем документ отклоняется до разбора текста.
 */
public class PdfBoxTextExtractor implements PdfTextExtractor {

    // Шестимесячная выписка Kaspi Gold занимает около 25 страниц; лимит защищает от разбора чрезмерно больших документов.
    private static final int DEFAULT_MAX_PAGES = 200;
    // Спецификация PDF допускает заголовок не в самом начале файла, но в пределах первых 1024 байт.
    private static final int SIGNATURE_SEARCH_LIMIT = 1024;
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final int maxPages;

    public PdfBoxTextExtractor() {
        this(DEFAULT_MAX_PAGES);
    }

    public PdfBoxTextExtractor(int maxPages) {
        if (maxPages <= 0) {
            throw new IllegalArgumentException("Максимальное число страниц должно быть положительным.");
        }
        this.maxPages = maxPages;
    }

    @Override
    public String extract(InputStream input) {
        byte[] content = readAll(input);
        if (!hasPdfSignature(content)) {
            throw new PdfExtractionException("Файл не является PDF-документом.", null);
        }

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(content))) {
            if (document.getNumberOfPages() > maxPages) {
                throw new PdfExtractionException("PDF-документ содержит больше " + maxPages + " страниц.", null);
            }
            return new PDFTextStripper().getText(document);
        } catch (InvalidPasswordException exception) {
            throw new PdfExtractionException("PDF-документ защищён паролем. Загрузите выписку без пароля.", exception);
        } catch (IOException exception) {
            throw new PdfExtractionException("Не удалось прочитать PDF-документ: файл повреждён или имеет неподдерживаемую структуру.", exception);
        }
    }

    private byte[] readAll(InputStream input) {
        try (InputStream source = input) {
            return source.readAllBytes();
        } catch (IOException exception) {
            throw new PdfExtractionException("Не удалось прочитать загруженный файл.", exception);
        }
    }

    private boolean hasPdfSignature(byte[] content) {
        int lastStart = Math.min(content.length, SIGNATURE_SEARCH_LIMIT) - PDF_SIGNATURE.length;
        for (int start = 0; start <= lastStart; start++) {
            if (matchesAt(content, start)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesAt(byte[] content, int start) {
        for (int index = 0; index < PDF_SIGNATURE.length; index++) {
            if (content[start + index] != PDF_SIGNATURE[index]) {
                return false;
            }
        }
        return true;
    }
}
