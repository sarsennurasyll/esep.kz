package com.esep.persistence.jpa.read.repository;

import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;

import java.math.BigDecimal;

/**
 * Проекция агрегата операций по типу движения и назначению.
 */
public interface MoneyFlowTotalProjection {

    MoneyFlowType getFlowType();

    MoneyPurpose getPurpose();

    BigDecimal getInflow();

    BigDecimal getOutflow();

    long getOperationCount();
}
