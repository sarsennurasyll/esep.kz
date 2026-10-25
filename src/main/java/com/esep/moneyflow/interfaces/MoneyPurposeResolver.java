package com.esep.moneyflow.interfaces;

import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.PurposeDecision;

import java.math.BigDecimal;

/** Определяет назначение новой операции по личным правилам пользователя. */
@FunctionalInterface
public interface MoneyPurposeResolver {

    PurposeDecision decide(String description, BigDecimal amount, MoneyFlowType flowType);
}
