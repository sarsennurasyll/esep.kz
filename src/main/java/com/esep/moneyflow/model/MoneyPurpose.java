package com.esep.moneyflow.model;

/**
 * Назначение операции. Выписка его почти никогда не содержит, поэтому UNKNOWN — нормальное состояние,
 * а не ошибка распознавания.
 */
public enum MoneyPurpose {
    INCOME_WORK,
    INCOME_REGULAR,
    EXPENSE,
    FAMILY,
    DEBT,
    OWN_FUNDS,
    GIFT,
    UNKNOWN
}
