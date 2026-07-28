package com.esep.statementimport.kaspi;

import com.esep.entity.BankType;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.pdf.PdfTextExtractor;

import java.io.InputStream;
import java.util.stream.IntStream;

/**
 * Начальная реализация парсера выписки Kaspi с поиском сырых строк операций.
 */
public class KaspiStatementParser implements StatementParser {

    private final PdfTextExtractor pdfTextExtractor;
    private final KaspiTransactionExtractor transactionExtractor;
    private final KaspiTransactionParser transactionParser;

    public KaspiStatementParser(PdfTextExtractor pdfTextExtractor) {
        this(pdfTextExtractor, new KaspiTransactionExtractor(), new KaspiTransactionParser());
    }

    KaspiStatementParser(
            PdfTextExtractor pdfTextExtractor,
            KaspiTransactionExtractor transactionExtractor,
            KaspiTransactionParser transactionParser
    ) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.transactionExtractor = transactionExtractor;
        this.transactionParser = transactionParser;
    }

    @Override
    public BankType supportedBank() {
        return BankType.KASPI;
    }

    @Override
    public ParsedStatement parse(InputStream input) {
        String statementText = pdfTextExtractor.extract(input);
        RawStatement rawStatement = transactionExtractor.extract(statementText);

        return new ParsedStatement(
                null,
                null,
                null,
                IntStream.range(0, rawStatement.transactionLines().size())
                        .mapToObj(position -> transactionParser.parse(
                                rawStatement.transactionLines().get(position),
                                position
                        ))
                        .toList()
        );
    }
}
