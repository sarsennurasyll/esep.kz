package com.esep.statementimport.kaspi;

import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.pdf.PdfTextExtractor;

import java.io.InputStream;
import java.util.List;

/**
 * Начальная реализация парсера выписки Kaspi с поиском сырых строк операций.
 */
public class KaspiStatementParser implements StatementParser {

    private final PdfTextExtractor pdfTextExtractor;
    private final KaspiTransactionExtractor transactionExtractor;

    public KaspiStatementParser(PdfTextExtractor pdfTextExtractor) {
        this(pdfTextExtractor, new KaspiTransactionExtractor());
    }

    KaspiStatementParser(PdfTextExtractor pdfTextExtractor, KaspiTransactionExtractor transactionExtractor) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.transactionExtractor = transactionExtractor;
    }

    @Override
    public ParsedStatement parse(InputStream input) {
        String statementText = pdfTextExtractor.extract(input);
        RawStatement rawStatement = transactionExtractor.extract(statementText);

        return new ParsedStatement(null, null, null, List.of());
    }
}
