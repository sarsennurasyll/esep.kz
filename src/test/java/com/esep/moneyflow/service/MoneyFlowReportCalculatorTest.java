package com.esep.moneyflow.service;

import com.esep.moneyflow.model.MoneyFlowReport;
import com.esep.moneyflow.model.MoneyFlowTotal;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyFlowReportCalculatorTest {

    private final MoneyFlowReportCalculator calculator = new MoneyFlowReportCalculator();

    @Test
    void shouldNotCountOwnCashAndUnlabeledTransfersAsIncomeOrExpense() {
        MoneyFlowReport report = calculator.calculate(List.of(
                total(MoneyFlowType.CASH_DEPOSIT, MoneyPurpose.UNKNOWN, "30000", "0"),
                total(MoneyFlowType.TRANSFER_FROM_PERSON, MoneyPurpose.UNKNOWN, "70000", "0"),
                total(MoneyFlowType.PURCHASE, MoneyPurpose.UNKNOWN, "0", "40000"),
                total(MoneyFlowType.PURCHASE_REFUND, MoneyPurpose.UNKNOWN, "1500", "0"),
                total(MoneyFlowType.BANK_FEE, MoneyPurpose.UNKNOWN, "0", "500"),
                total(MoneyFlowType.TRANSFER_TO_PERSON, MoneyPurpose.UNKNOWN, "0", "35000"),
                total(MoneyFlowType.CASH_WITHDRAWAL, MoneyPurpose.UNKNOWN, "0", "20000")
        ));

        assertThat(report.income()).isEqualByComparingTo("0");
        assertThat(report.expense()).isEqualByComparingTo("39000");
        assertThat(report.internalInflow()).isEqualByComparingTo("30000");
        assertThat(report.unknownInflow()).isEqualByComparingTo("70000");
        assertThat(report.unknownOutflow()).isEqualByComparingTo("35000");
        assertThat(report.cashWithdrawals()).isEqualByComparingTo("20000");
        assertThat(report.totalInflow()).isEqualByComparingTo("101500");
        assertThat(report.totalOutflow()).isEqualByComparingTo("95500");
        assertThat(report.unknownInflowShare()).isEqualByComparingTo("0.6897");
        assertThat(report.unknownOutflowShare()).isEqualByComparingTo("0.5759");
    }

    @Test
    void shouldFollowUserPurposeOverFlowType() {
        MoneyFlowReport report = calculator.calculate(List.of(
                total(MoneyFlowType.TRANSFER_FROM_PERSON, MoneyPurpose.INCOME_WORK, "50000", "0"),
                total(MoneyFlowType.TRANSFER_FROM_PERSON, MoneyPurpose.DEBT, "10000", "0"),
                total(MoneyFlowType.TRANSFER_TO_PERSON, MoneyPurpose.DEBT, "0", "10000"),
                total(MoneyFlowType.TRANSFER_TO_PERSON, MoneyPurpose.FAMILY, "0", "15000"),
                total(MoneyFlowType.TRANSFER_TO_PERSON, MoneyPurpose.EXPENSE, "0", "8000"),
                total(MoneyFlowType.TRANSFER_FROM_PERSON, MoneyPurpose.OWN_FUNDS, "25000", "0")
        ));

        assertThat(report.income()).isEqualByComparingTo("50000");
        assertThat(report.internalInflow()).isEqualByComparingTo("35000");
        assertThat(report.internalOutflow()).isEqualByComparingTo("10000");
        assertThat(report.familyOutflow()).isEqualByComparingTo("15000");
        assertThat(report.expense()).isEqualByComparingTo("8000");
        assertThat(report.unknownInflow()).isEqualByComparingTo("0");
        assertThat(report.unknownOutflow()).isEqualByComparingTo("0");
    }

    @Test
    void shouldAccountEveryAmountExactlyOnce() {
        List<MoneyFlowTotal> totals = List.of(
                total(MoneyFlowType.PURCHASE, MoneyPurpose.UNKNOWN, "0", "1000"),
                total(MoneyFlowType.PURCHASE_REFUND, MoneyPurpose.UNKNOWN, "200", "0"),
                total(MoneyFlowType.UNKNOWN, MoneyPurpose.UNKNOWN, "300", "400"),
                total(MoneyFlowType.CASH_DEPOSIT, MoneyPurpose.UNKNOWN, "500", "0"),
                total(MoneyFlowType.TRANSFER_TO_PERSON, MoneyPurpose.GIFT, "0", "600"),
                total(MoneyFlowType.TRANSFER_FROM_PERSON, MoneyPurpose.INCOME_REGULAR, "700", "0"),
                total(MoneyFlowType.CASH_WITHDRAWAL, MoneyPurpose.UNKNOWN, "0", "800")
        );

        MoneyFlowReport report = calculator.calculate(totals);

        BigDecimal accountedInflow = report.income().add(report.familyInflow()).add(report.internalInflow())
                .add(report.unknownInflow());
        BigDecimal accountedOutflow = report.expense().add(report.cashWithdrawals()).add(report.familyOutflow())
                .add(report.internalOutflow()).add(report.unknownOutflow());
        // Возвраты покупок учтены внутри расхода со знаком минус.
        assertThat(accountedInflow.add(new BigDecimal("200"))).isEqualByComparingTo(report.totalInflow());
        assertThat(accountedOutflow.add(new BigDecimal("200"))).isEqualByComparingTo(report.totalOutflow());
    }

    @Test
    void shouldReturnZeroSharesForEmptyData() {
        MoneyFlowReport report = calculator.calculate(List.of());

        assertThat(report.unknownInflowShare()).isEqualByComparingTo("0");
        assertThat(report.unknownOutflowShare()).isEqualByComparingTo("0");
    }

    private MoneyFlowTotal total(MoneyFlowType flowType, MoneyPurpose purpose, String inflow, String outflow) {
        return new MoneyFlowTotal(flowType, purpose, new BigDecimal(inflow), new BigDecimal(outflow), 1);
    }
}
