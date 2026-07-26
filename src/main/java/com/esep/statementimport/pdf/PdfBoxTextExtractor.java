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

    @Override
    public String extract(InputStream input) {
        try (
                InputStream source = input;
                RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(source);
                PDDocument document = Loader.loadPDF(buffer)
        ) {
            return new PDFTextStripper().getText(document);
        } catch (IOException exception) {
            throw new PdfExtractionException("Не удалось извлечь текст из PDF-документа.", exception);
        }
    }
}
