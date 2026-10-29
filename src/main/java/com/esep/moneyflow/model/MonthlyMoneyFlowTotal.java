package com.esep.moneyflow.model;

import java.time.YearMonth;
import java.util.Objects;

/** Агрегат операций одного месяца, типа движения и назначения. */
public record MonthlyMoneyFlowTotal(YearMonth month, MoneyFlowTotal total) {

    public MonthlyMoneyFlowTotal {
        Objects.requireNonNull(month, "Month must not be null");
        Objects.requireNonNull(total, "Total must not be null");
    }
}
