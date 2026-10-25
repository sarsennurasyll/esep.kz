package com.esep.moneyflow.service;

import com.esep.entity.BankOperationType;
import com.esep.moneyflow.model.ClassificationConfidence;
import com.esep.moneyflow.model.MoneyFlowClassification;
import com.esep.moneyflow.model.MoneyFlowType;

import java.math.BigDecimal;

/**
 * Общие правила, которые следуют только из документального типа операции и направления.
 * Применяются для любого банка; правила конкретного банка могут их уточнить.
 */
public final class BaseMoneyFlowRules {

    private BaseMoneyFlowRules() {
    }

    public static MoneyFlowClassification classify(BankOperationType bankOperationType, BigDecimal amount) {
        boolean outgoing = amount.signum() < 0;
        return switch (bankOperationType) {
            case PURCHASE -> MoneyFlowClassification.byBankRule(
                    outgoing ? MoneyFlowType.PURCHASE : MoneyFlowType.PURCHASE_REFUND, ClassificationConfidence.HIGH);
            case CASH_WITHDRAWAL -> outgoing
                    ? MoneyFlowClassification.byBankRule(MoneyFlowType.CASH_WITHDRAWAL, ClassificationConfidence.HIGH)
                    : MoneyFlowClassification.unknown();
            // Перевод может уйти и на собственный счёт в другом банке, поэтому уверенность средняя.
            case TRANSFER -> outgoing
                    ? MoneyFlowClassification.byBankRule(MoneyFlowType.TRANSFER_TO_PERSON, ClassificationConfidence.MEDIUM)
                    : MoneyFlowClassification.byBankRule(MoneyFlowType.TRANSFER_FROM_PERSON, ClassificationConfidence.MEDIUM);
            // «Разное» у банков обычно означает комиссии, но может включать и другие списания.
            case OTHER -> outgoing
                    ? MoneyFlowClassification.byBankRule(MoneyFlowType.BANK_FEE, ClassificationConfidence.MEDIUM)
                    : MoneyFlowClassification.unknown();
            case TOP_UP, UNKNOWN -> MoneyFlowClassification.unknown();
        };
    }
}
