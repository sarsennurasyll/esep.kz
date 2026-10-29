package com.esep.moneyflow.service;

import com.esep.moneyflow.model.MoneyFlowReport;
import com.esep.moneyflow.model.MoneyFlowTotal;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.moneyflow.model.MonthlyMoneyFlowReport;
import com.esep.moneyflow.model.MonthlyMoneyFlowTotal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Строит отчёт о движении денег по правилам ADR-0003. Решение пользователя о назначении важнее
 * типа движения; суммы, назначение которых неизвестно, не попадают ни в доход, ни в расход.
 */
public class MoneyFlowReportCalculator {

    private static final Set<MoneyPurpose> INCOME = Set.of(MoneyPurpose.INCOME_WORK, MoneyPurpose.INCOME_REGULAR);
    private static final Set<MoneyPurpose> INTERNAL = Set.of(MoneyPurpose.OWN_FUNDS, MoneyPurpose.DEBT);
    private static final Set<MoneyPurpose> FAMILY = Set.of(MoneyPurpose.FAMILY, MoneyPurpose.GIFT);
    private static final Set<MoneyFlowType> INTERNAL_FLOWS = Set.of(MoneyFlowType.CASH_DEPOSIT, MoneyFlowType.OWN_ACCOUNT_TRANSFER);
    private static final Set<MoneyFlowType> SPENDING_FLOWS = Set.of(
            MoneyFlowType.PURCHASE, MoneyFlowType.PURCHASE_REFUND, MoneyFlowType.BANK_FEE
    );
    private static final int SHARE_SCALE = 4;

    public MoneyFlowReport calculate(List<MoneyFlowTotal> totals) {
        Accumulator report = new Accumulator();
        totals.forEach(report::add);
        return report.toReport();
    }

    /** Отчёт по каждому месяцу, в котором есть операции, в хронологическом порядке. */
    public List<MonthlyMoneyFlowReport> calculateMonthly(List<MonthlyMoneyFlowTotal> totals) {
        Map<YearMonth, List<MoneyFlowTotal>> byMonth = new TreeMap<>();
        totals.forEach(total -> byMonth.computeIfAbsent(total.month(), ignored -> new ArrayList<>()).add(total.total()));
        return byMonth.entrySet().stream()
                .map(entry -> new MonthlyMoneyFlowReport(entry.getKey(), calculate(entry.getValue())))
                .toList();
    }

    private static final class Accumulator {
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;
        private BigDecimal cashWithdrawals = BigDecimal.ZERO;
        private BigDecimal familyInflow = BigDecimal.ZERO;
        private BigDecimal familyOutflow = BigDecimal.ZERO;
        private BigDecimal internalInflow = BigDecimal.ZERO;
        private BigDecimal internalOutflow = BigDecimal.ZERO;
        private BigDecimal unknownInflow = BigDecimal.ZERO;
        private BigDecimal unknownOutflow = BigDecimal.ZERO;
        private BigDecimal totalInflow = BigDecimal.ZERO;
        private BigDecimal totalOutflow = BigDecimal.ZERO;

        void add(MoneyFlowTotal total) {
            BigDecimal in = total.inflow();
            BigDecimal out = total.outflow();
            totalInflow = totalInflow.add(in);
            totalOutflow = totalOutflow.add(out);

            MoneyPurpose purpose = total.purpose();
            MoneyFlowType flowType = total.flowType();
            if (INTERNAL.contains(purpose) || INTERNAL_FLOWS.contains(flowType)) {
                internalInflow = internalInflow.add(in);
                internalOutflow = internalOutflow.add(out);
            } else if (FAMILY.contains(purpose)) {
                familyInflow = familyInflow.add(in);
                familyOutflow = familyOutflow.add(out);
            } else if (INCOME.contains(purpose)) {
                income = income.add(in);
                unknownOutflow = unknownOutflow.add(out);
            } else if (purpose == MoneyPurpose.EXPENSE || SPENDING_FLOWS.contains(flowType)) {
                // Возврат покупки уменьшает расход, а не считается доходом.
                expense = expense.add(out).subtract(in);
            } else if (flowType == MoneyFlowType.CASH_WITHDRAWAL) {
                cashWithdrawals = cashWithdrawals.add(out);
                unknownInflow = unknownInflow.add(in);
            } else {
                unknownInflow = unknownInflow.add(in);
                unknownOutflow = unknownOutflow.add(out);
            }
        }

        MoneyFlowReport toReport() {
            return new MoneyFlowReport(
                    income, expense, cashWithdrawals, familyInflow, familyOutflow, internalInflow, internalOutflow,
                    unknownInflow, unknownOutflow, totalInflow, totalOutflow,
                    share(unknownInflow, totalInflow),
                    share(unknownOutflow.add(cashWithdrawals), totalOutflow)
            );
        }

        private BigDecimal share(BigDecimal part, BigDecimal whole) {
            return whole.signum() == 0 ? BigDecimal.ZERO.setScale(SHARE_SCALE) : part.divide(whole, SHARE_SCALE, RoundingMode.HALF_UP);
        }
    }
}
