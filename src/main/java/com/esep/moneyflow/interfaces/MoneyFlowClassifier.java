package com.esep.moneyflow.interfaces;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.moneyflow.model.MoneyFlowClassification;

import java.math.BigDecimal;

/**
 * Правила определения типа движения денег для выписок конкретного банка.
 */
public interface MoneyFlowClassifier {

    BankType supportedBank();

    MoneyFlowClassification classify(BankOperationType bankOperationType, BigDecimal amount, String description);
}
