package com.esep.statementimport.pdf;

import java.time.Duration;
import java.util.Objects;

/**
 * Ограничения обработки одного PDF-документа.
 *
 * @param maxPages          максимальное число страниц
 * @param maxTextCharacters максимальный объём извлечённого текста
 * @param maxDuration       максимальное время разбора; проверяется после загрузки документа и после каждой страницы
 * @param maxMemoryBytes    лимит памяти буферов PDFBox для распакованных потоков
 */
public record PdfProcessingLimits(int maxPages, int maxTextCharacters, Duration maxDuration, long maxMemoryBytes) {

    // Шестимесячная выписка Kaspi Gold — около 25 страниц и 60 тысяч символов; лимиты оставляют запас в несколько раз.
    public static final PdfProcessingLimits DEFAULT = new PdfProcessingLimits(200, 2_000_000, Duration.ofSeconds(20), 64L * 1024 * 1024);

    public PdfProcessingLimits {
        Objects.requireNonNull(maxDuration, "Max duration must not be null");
        if (maxPages <= 0 || maxTextCharacters <= 0 || maxMemoryBytes <= 0 || maxDuration.isNegative() || maxDuration.isZero()) {
            throw new IllegalArgumentException("PDF processing limits must be positive");
        }
    }

    public PdfProcessingLimits withMaxPages(int pages) {
        return new PdfProcessingLimits(pages, maxTextCharacters, maxDuration, maxMemoryBytes);
    }
}
