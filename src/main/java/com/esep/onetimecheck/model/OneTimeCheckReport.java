package com.esep.onetimecheck.model;

import com.esep.analytics.model.MonthlyMoneyFlow;
import com.esep.moneyflow.model.MoneyFlowReport;

import java.time.LocalDate;
import java.util.List;

/**
 * Отчёт разовой проверки выписки. Существует только в ответе на загрузку и нигде не сохраняется (ADR-0007).
 *
 * @param purchasesWithMerchant покупки и возвраты покупок, у которых найден продавец в общей базе
 * @param purchasesWithoutMerchant покупки и возвраты покупок без продавца
 */
public record OneTimeCheckReport(
        LocalDate periodFrom,
        LocalDate periodTo,
        int operationCount,
        MoneyFlowReport moneyFlow,
        List<MonthlyMoneyFlow> monthlyMoneyFlow,
        int purchasesWithMerchant,
        int purchasesWithoutMerchant
) {

    public OneTimeCheckReport {
        monthlyMoneyFlow = List.copyOf(monthlyMoneyFlow);
    }
}
