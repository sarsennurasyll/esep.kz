package com.esep.persistence.jpa.read.repository;

import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.MoneyPurpose;

import java.math.BigDecimal;

/** Проекция операций одного описания контрагента и назначения. */
public interface CounterpartyActivityProjection {

    String getDescription();

    MoneyPurpose getPurpose();

    ClassificationSource getPurposeSource();

    BigDecimal getAmount();

    long getOperationCount();
}
