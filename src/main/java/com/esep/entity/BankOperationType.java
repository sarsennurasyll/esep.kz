package com.esep.entity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Документальный тип банковской операции из выписки. */
public enum BankOperationType {
    PURCHASE(true),
    TRANSFER(false),
    TOP_UP(false),
    CASH_WITHDRAWAL(false),
    OTHER(false),
    // Неизвестный тип включает операции, импортированные до появления типов, поэтому продавец возможен.
    UNKNOWN(true);

    private final boolean merchantCounterpartyPossible;

    BankOperationType(boolean merchantCounterpartyPossible) {
        this.merchantCounterpartyPossible = merchantCounterpartyPossible;
    }

    /**
     * Показывает, может ли контрагентом операции быть продавец из базы знаний.
     * Переводы, пополнения и снятие наличных описывают людей, собственные счета или устройства, а не продавцов.
     */
    public boolean merchantCounterpartyPossible() {
        return merchantCounterpartyPossible;
    }

    public static Set<BankOperationType> withPossibleMerchantCounterparty() {
        return Arrays.stream(values())
                .filter(BankOperationType::merchantCounterpartyPossible)
                .collect(Collectors.toUnmodifiableSet());
    }
}
