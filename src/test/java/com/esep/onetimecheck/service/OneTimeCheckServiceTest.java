package com.esep.onetimecheck.service;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.merchantmanagement.interfaces.MerchantReadQuery;
import com.esep.merchantmanagement.model.MerchantSummary;
import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantrecognition.model.MerchantRecognitionResult;
import com.esep.merchantresolver.model.MerchantMatch;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.moneyflow.service.MoneyFlowClassifierRegistry;
import com.esep.onetimecheck.model.OneTimeCheckReport;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.interfaces.StatementParserRegistry;
import com.esep.statementimport.kaspi.KaspiMoneyFlowClassifier;
import com.esep.statementimport.model.ParsedStatement;
import com.esep.statementimport.model.ParsedTransaction;
import com.esep.statementimport.service.StatementPeriodResolver;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OneTimeCheckServiceTest {

    @Test
    void shouldBuildMoneyFlowReportInMemory() {
        StatementParser parser = mock(StatementParser.class);
        when(parser.parse(any())).thenReturn(new ParsedStatement("1234", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 15), List.of(
                operation(LocalDate.of(2026, 7, 3), "TEST STORE", "-1000", BankOperationType.PURCHASE),
                operation(LocalDate.of(2026, 7, 4), "UNKNOWN SHOP", "-500", BankOperationType.PURCHASE),
                operation(LocalDate.of(2026, 7, 5), "ИП Gamer", "-4000", BankOperationType.PURCHASE),
                operation(LocalDate.of(2026, 7, 6), "ИП Gamer", "1000", BankOperationType.PURCHASE),
                operation(LocalDate.of(2026, 7, 10), "Тестов Т.", "20000", BankOperationType.TOP_UP),
                operation(LocalDate.of(2026, 8, 2), "Банкомат TEST", "-3000", BankOperationType.CASH_WITHDRAWAL)
        )));
        StatementParserRegistry registry = mock(StatementParserRegistry.class);
        when(registry.getParser(BankType.KASPI)).thenReturn(parser);
        MerchantRecognitionService recognition = mock(MerchantRecognitionService.class);
        when(recognition.recognize("TEST STORE")).thenReturn(new MerchantRecognitionResult("TEST STORE", "TEST STORE",
                MerchantMatch.matched(new MerchantReference("1"), "TEST STORE", 1.0, true)));
        when(recognition.recognize("ИП Gamer")).thenReturn(new MerchantRecognitionResult("ИП Gamer", "GAMER",
                MerchantMatch.matched(new MerchantReference("2"), "GAMER", 1.0, true)));
        MerchantReadQuery merchants = () -> List.of(
                new MerchantSummary(new MerchantReference("1"), "TEST STORE", "Продукты"),
                new MerchantSummary(new MerchantReference("2"), "ИП Gamer", "Игры"));
        when(recognition.recognize("UNKNOWN SHOP")).thenReturn(new MerchantRecognitionResult("UNKNOWN SHOP", "UNKNOWN SHOP", MerchantMatch.notMatched()));
        OneTimeCheckService service = new OneTimeCheckService(registry, new StatementPeriodResolver(),
                new MoneyFlowClassifierRegistry(List.of(new KaspiMoneyFlowClassifier())), recognition, merchants);

        OneTimeCheckReport report = service.check(new ByteArrayInputStream(new byte[0]), BankType.KASPI);

        assertThat(report.operationCount()).isEqualTo(6);
        assertThat(report.periodFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(report.moneyFlow().expense()).isEqualByComparingTo("4500");
        assertThat(report.moneyFlow().cashWithdrawals()).isEqualByComparingTo("3000");
        assertThat(report.moneyFlow().income()).isEqualByComparingTo("0");
        assertThat(report.moneyFlow().unknownInflow()).isEqualByComparingTo("20000");
        assertThat(report.purchasesWithMerchant()).isEqualTo(3);
        assertThat(report.purchasesWithoutMerchant()).isEqualTo(1);
        assertThat(report.monthlyMoneyFlow()).extracting(month -> month.month() + ":" + month.complete())
                .containsExactly("2026-07:true", "2026-08:false");
        assertThat(report.categorySpending()).extracting(spending -> spending.category() + ":" + spending.amount().toPlainString())
                .containsExactly("Игры:3000", "Продукты:1000", "Без категории:500");
    }

    private static ParsedTransaction operation(LocalDate date, String description, String amount, BankOperationType type) {
        return new ParsedTransaction(date, description, new BigDecimal(amount), "KZT", type, 0);
    }
}
