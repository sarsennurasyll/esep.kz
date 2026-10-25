package com.esep.moneyflow.model;

import java.util.Objects;

/**
 * Решение о типе движения денег вместе с его источником и уверенностью.
 */
public record MoneyFlowClassification(
        MoneyFlowType flowType,
        ClassificationSource source,
        ClassificationConfidence confidence
) {

    public MoneyFlowClassification {
        Objects.requireNonNull(flowType, "Flow type must not be null");
        Objects.requireNonNull(source, "Source must not be null");
        Objects.requireNonNull(confidence, "Confidence must not be null");
    }

    public static MoneyFlowClassification byBankRule(MoneyFlowType flowType, ClassificationConfidence confidence) {
        return new MoneyFlowClassification(flowType, ClassificationSource.BANK_RULE, confidence);
    }

    public static MoneyFlowClassification unknown() {
        return new MoneyFlowClassification(MoneyFlowType.UNKNOWN, ClassificationSource.NONE, ClassificationConfidence.NONE);
    }
}
