package com.esep.persistence.jpa.read.repository;

/**
 * Проекция агрегированного описания операции без продавца.
 */
public interface UnknownMerchantDescriptionProjection {

    String getDescription();

    long getUsageCount();

    java.math.BigDecimal getTotalAmount();

    java.time.LocalDate getLastTransactionDate();

    boolean getNewInLatestStatement();
}
