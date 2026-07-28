package com.esep.persistence.jpa.read.repository;

import java.math.BigDecimal;

/**
 * Проекция исходящих переводов физическим лицам.
 */
public interface PersonTransferProjection {

    String getRecipient();

    BigDecimal getAmount();

    long getTransactionCount();
}
