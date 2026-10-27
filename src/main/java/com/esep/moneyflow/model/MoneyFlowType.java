package com.esep.moneyflow.model;

/**
 * Экономический тип движения денег. Определяется правилами банка и решением пользователя,
 * а не только знаком суммы.
 */
public enum MoneyFlowType {
    PURCHASE,
    PURCHASE_REFUND,
    TRANSFER_TO_PERSON,
    TRANSFER_FROM_PERSON,
    CASH_DEPOSIT,
    CASH_WITHDRAWAL,
    OWN_ACCOUNT_TRANSFER,
    BANK_FEE,
    UNKNOWN;

    /** Назначение операций этого типа выписка не содержит, и его может указать только пользователь. */
    public boolean isPurposeLabelable() {
        return this == TRANSFER_TO_PERSON || this == TRANSFER_FROM_PERSON || this == UNKNOWN;
    }
}
