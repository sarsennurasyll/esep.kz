package com.esep.entity;

/** Документальный тип банковской операции из выписки. */
public enum BankOperationType {
    PURCHASE,
    TRANSFER,
    TOP_UP,
    CASH_WITHDRAWAL,
    OTHER,
    UNKNOWN
}
