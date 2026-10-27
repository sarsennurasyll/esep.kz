package com.esep.statementimport.pdf;

import com.esep.statementimport.model.ImportErrorCode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.LongSupplier;

/**
 * Извлекает текст из PDF-документа с помощью Apache PDFBox.
 * Файл без сигнатуры PDF, повреждённый или защищённый паролем документ отклоняется до разбора текста.
 * Разбор идёт постранично: после каждой страницы проверяются срок и объём текста, поэтому превышение
 * лимита действительно останавливает обработку, а не только прерывает ожидание результата.
 */
public class PdfBoxTextExtractor implements PdfTextExtractor {

    // Спецификация PDF допускает заголовок не в самом начале файла, но в пределах первых 1024 байт.
    private static final int SIGNATURE_SEARCH_LIMIT = 1024;
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    // Так PDFBox сообщает о превышении лимита памяти буферов (ScratchFile).
    private static final String MEMORY_LIMIT_MESSAGE = "Maximum allowed";

    private final PdfProcessingLimits limits;
    private final LongSupplier nanoClock;

    public PdfBoxTextExtractor() {
        this(PdfProcessingLimits.DEFAULT);
    }

    public PdfBoxTextExtractor(int maxPages) {
        this(PdfProcessingLimits.DEFAULT.withMaxPages(maxPages));
    }

    public PdfBoxTextExtractor(PdfProcessingLimits limits) {
        this(limits, System::nanoTime);
    }

    PdfBoxTextExtractor(PdfProcessingLimits limits, LongSupplier nanoClock) {
        this.limits = limits;
        this.nanoClock = nanoClock;
    }

    @Override
    public String extract(InputStream input) {
        long deadline = nanoClock.getAsLong() + limits.maxDuration().toNanos();
        byte[] content = readAll(input);
        if (!hasPdfSignature(content)) {
            throw new PdfExtractionException(ImportErrorCode.PDF_NOT_PDF, "Файл не является PDF-документом.", null);
        }

        MemoryUsageSetting memory = MemoryUsageSetting.setupMainMemoryOnly(limits.maxMemoryBytes());
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(content), memory.streamCache)) {
            int pages = document.getNumberOfPages();
            if (pages > limits.maxPages()) {
                throw new PdfLimitExceededException(ImportErrorCode.PDF_TOO_MANY_PAGES, "PDF-документ содержит больше " + limits.maxPages() + " страниц.");
            }
            checkDeadline(deadline);
            return extractPages(document, pages, deadline);
        } catch (InvalidPasswordException exception) {
            throw new PdfExtractionException(ImportErrorCode.PDF_ENCRYPTED, "PDF-документ защищён паролем. Загрузите выписку без пароля.", exception);
        } catch (IOException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains(MEMORY_LIMIT_MESSAGE)) {
                throw new PdfLimitExceededException(ImportErrorCode.PDF_TOO_COMPLEX, "PDF-документ требует слишком много памяти для обработки.");
            }
            throw new PdfExtractionException(ImportErrorCode.PDF_CORRUPTED, "Не удалось прочитать PDF-документ: файл повреждён или имеет неподдерживаемую структуру.", exception);
        }
    }

    private String extractPages(PDDocument document, int pages, long deadline) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        StringBuilder text = new StringBuilder();
        for (int page = 1; page <= pages; page++) {
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            text.append(stripper.getText(document));
            if (text.length() > limits.maxTextCharacters()) {
                throw new PdfLimitExceededException(ImportErrorCode.PDF_TOO_MUCH_TEXT, "PDF-документ содержит слишком много текста для выписки.");
            }
            checkDeadline(deadline);
        }
        return text.toString();
    }

    private void checkDeadline(long deadline) {
        if (nanoClock.getAsLong() - deadline > 0) {
            throw new PdfProcessingTimeoutException("Обработка PDF-документа заняла слишком много времени и была остановлена.");
        }
    }

    private byte[] readAll(InputStream input) {
        try (InputStream source = input) {
            return source.readAllBytes();
        } catch (IOException exception) {
            throw new PdfExtractionException(ImportErrorCode.PDF_CORRUPTED, "Не удалось прочитать загруженный файл.", exception);
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
