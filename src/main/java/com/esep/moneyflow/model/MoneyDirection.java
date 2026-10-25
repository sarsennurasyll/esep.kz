package com.esep.moneyflow.model;

import java.math.BigDecimal;

/** Направление движения денег относительно счёта владельца выписки. */
public enum MoneyDirection {
    INFLOW("Поступления"),
    OUTFLOW("Списания");

    private final String displayName;

    MoneyDirection(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static MoneyDirection of(BigDecimal amount) {
        return amount.signum() > 0 ? INFLOW : OUTFLOW;
    }

    /** Проверяет, может ли назначение относиться к этому направлению. */
    public boolean allows(MoneyPurpose purpose) {
        return switch (purpose) {
            case INCOME_WORK, INCOME_REGULAR -> this == INFLOW;
            case EXPENSE -> this == OUTFLOW;
            case FAMILY, DEBT, OWN_FUNDS, GIFT, UNKNOWN -> true;
        };
    }
}
