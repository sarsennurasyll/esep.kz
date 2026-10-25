package com.esep.moneyflow.model;

/** Кто принял решение о типе движения или назначении операции. */
public enum ClassificationSource {
    BANK_RULE,
    MERCHANT_KB,
    PERSONAL_RULE,
    USER,
    NONE
}
