package com.esep.persistence.jpa.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Операции, привязанные к продавцу: число, сумма по модулю и даты первой и последней.
 */
public interface MerchantActivityProjection {

    Long getMerchantId();

    long getTransactionCount();

    BigDecimal getTotalAmount();

    LocalDate getFirstTransactionDate();

    LocalDate getLastTransactionDate();
}
