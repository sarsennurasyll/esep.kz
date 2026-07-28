package com.esep.merchantmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Сырой агрегат неизвестного описания из хранилища операций.
 */
public record UnknownMerchantCandidate(
        String description,
        long usageCount,
        BigDecimal totalAmount,
        LocalDate lastTransactionDate,
        boolean newInLatestStatement
) {
    public UnknownMerchantCandidate(String description, long usageCount) {
        this(description, usageCount, BigDecimal.ZERO, LocalDate.MIN, false);
    }
}
