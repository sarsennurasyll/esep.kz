package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.statementimport.exception.StatementFormatException;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;
import com.esep.statementimport.pdf.PdfTextExtractor;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Парсер выписки Kaspi Gold: извлекает операции, период из шапки и сверяет суммы с итогами шапки.
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

        List<ParsedTransaction> transactions = IntStream.range(0, rawStatement.transactionLines().size())
                .mapToObj(position -> transactionParser.parse(
                        rawStatement.transactionLines().get(position),
                        position
                ))
                .toList();

        verifyTransactionsWithinPeriod(rawStatement, transactions);
        verifySummaryTotals(rawStatement.summaryTotals(), transactions);

        return new ParsedStatement(rawStatement.cardNumberSuffix(), rawStatement.periodFrom(), rawStatement.periodTo(), transactions);
    }

    private void verifyTransactionsWithinPeriod(RawStatement rawStatement, List<ParsedTransaction> transactions) {
        if (rawStatement.periodFrom() == null || rawStatement.periodTo() == null) {
            return;
        }

        boolean outsidePeriod = transactions.stream().anyMatch(transaction ->
                transaction.date().isBefore(rawStatement.periodFrom()) || transaction.date().isAfter(rawStatement.periodTo())
        );
        if (outsidePeriod) {
            throw new StatementFormatException("Дата операции выходит за период, указанный в шапке выписки");
        }
    }

    // Итоги шапки — независимая контрольная сумма банка: расхождение означает пропущенную или лишнюю строку.
    private void verifySummaryTotals(Map<BankOperationType, BigDecimal> summaryTotals, List<ParsedTransaction> transactions) {
        summaryTotals.forEach((operationType, expectedTotal) -> {
            BigDecimal actualTotal = transactions.stream()
                    .filter(transaction -> transaction.bankOperationType() == operationType)
                    .map(ParsedTransaction::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (actualTotal.compareTo(expectedTotal) != 0) {
                throw new StatementFormatException(
                        "Сумма операций типа " + operationType + " не совпадает с итогом в шапке выписки"
                );
            }
        });
    }
}
