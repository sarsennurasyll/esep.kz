package com.esep.persistence.jpa.read.repository;

import java.math.BigDecimal;

/**
 * Проекция агрегированных расходов у продавца.
 */
public interface MerchantExpenseProjection {

    String getMerchant();

    BigDecimal getAmount();

    long getTransactionCount();
}
