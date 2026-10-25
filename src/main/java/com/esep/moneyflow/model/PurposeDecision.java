package com.esep.moneyflow.model;

/** Назначение операции и его источник. */
public record PurposeDecision(MoneyPurpose purpose, ClassificationSource source) {

    public static PurposeDecision unknown() {
        return new PurposeDecision(MoneyPurpose.UNKNOWN, ClassificationSource.NONE);
    }
}
