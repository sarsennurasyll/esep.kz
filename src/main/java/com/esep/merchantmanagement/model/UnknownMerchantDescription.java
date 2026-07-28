package com.esep.merchantmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Нормализованное неизвестное описание для подтверждения пользователем.
 */
public record UnknownMerchantDescription(
        String normalizedDescription,
        long usageCount,
        BigDecimal totalAmount,
        LocalDate lastTransactionDate,
        String exampleDescription,
        boolean newInLatestStatement,
        MerchantSuggestion suggestion
) {
    public UnknownMerchantDescription(String normalizedDescription, long usageCount, String exampleDescription) {
        this(normalizedDescription, usageCount, BigDecimal.ZERO, LocalDate.MIN, exampleDescription, false, null);
    }
}
