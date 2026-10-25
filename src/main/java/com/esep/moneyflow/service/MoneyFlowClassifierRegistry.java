package com.esep.moneyflow.service;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.moneyflow.interfaces.MoneyFlowClassifier;
import com.esep.moneyflow.model.MoneyFlowClassification;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Выбирает правила банка; для банка без собственных правил применяет общие.
 */
public class MoneyFlowClassifierRegistry {

    private final Map<BankType, MoneyFlowClassifier> classifiers = new EnumMap<>(BankType.class);

    public MoneyFlowClassifierRegistry(List<MoneyFlowClassifier> classifiers) {
        for (MoneyFlowClassifier classifier : classifiers) {
            if (this.classifiers.putIfAbsent(classifier.supportedBank(), classifier) != null) {
                throw new IllegalArgumentException("More than one money flow classifier is registered for bank: " + classifier.supportedBank());
            }
        }
    }

    public MoneyFlowClassification classify(BankType bankType, BankOperationType bankOperationType, BigDecimal amount, String description) {
        MoneyFlowClassifier classifier = classifiers.get(bankType);
        return classifier == null
                ? BaseMoneyFlowRules.classify(bankOperationType, amount)
                : classifier.classify(bankOperationType, amount, description);
    }
}
