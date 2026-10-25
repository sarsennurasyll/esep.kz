package com.esep.moneyflow.model;

/**
 * Назначение операции. Выписка его почти никогда не содержит, поэтому UNKNOWN — нормальное состояние,
 * а не ошибка распознавания.
 */
public enum MoneyPurpose {
    INCOME_WORK("Доход за работу, товары или услуги"),
    INCOME_REGULAR("Зарплата, пенсия, пособие"),
    EXPENSE("Расход"),
    FAMILY("Семья и близкие"),
    DEBT("Долг: выдан или возвращён"),
    OWN_FUNDS("Мои собственные деньги"),
    GIFT("Подарок"),
    UNKNOWN("Не знаю");

    private final String displayName;

    MoneyPurpose(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
