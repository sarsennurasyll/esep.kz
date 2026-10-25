package com.esep.statementimport.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Извлекает текст из PDF-документа с помощью Apache PDFBox.
 */
public class PdfBoxTextExtractor implements PdfTextExtractor {

    // Шестимесячная выписка Kaspi Gold занимает около 25 страниц; лимит защищает от разбора чрезмерно больших документов.
    private static final int DEFAULT_MAX_PAGES = 200;

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
        try (
                InputStream source = input;
                RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(source);
                PDDocument document = Loader.loadPDF(buffer)
        ) {
            if (document.getNumberOfPages() > maxPages) {
                throw new PdfExtractionException("PDF-документ содержит больше " + maxPages + " страниц.", null);
            }
            return new PDFTextStripper().getText(document);
        } catch (IOException exception) {
            throw new PdfExtractionException("Не удалось извлечь текст из PDF-документа.", exception);
        }
    }
}
