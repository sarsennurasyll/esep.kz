package com.esep.persistence.jpa.read.repository;

import com.esep.entity.MerchantType;

import java.math.BigDecimal;

/**
 * Проекция расходов по типу получателя платежа.
 */
public interface MerchantTypeExpenseProjection {

    MerchantType getMerchantType();

    BigDecimal getAmount();

    long getTransactionCount();
}
