package com.esep.merchantmanagement.api.dto;

import com.esep.merchantmanagement.model.UnknownMerchantDescription;

/**
 * HTTP-представление неизвестного описания операции.
 */
public record UnknownMerchantResponse(
        String normalizedDescription,
        long usageCount,
        java.math.BigDecimal totalAmount,
        java.time.LocalDate lastTransactionDate,
        String exampleDescription,
        boolean newInLatestStatement,
        MerchantResponse suggestion
) {

    public UnknownMerchantResponse(String normalizedDescription, long usageCount, String exampleDescription) {
        this(normalizedDescription, usageCount, java.math.BigDecimal.ZERO, java.time.LocalDate.MIN, exampleDescription, false, null);
    }

    public static UnknownMerchantResponse from(UnknownMerchantDescription description) {
        return new UnknownMerchantResponse(
                description.normalizedDescription(),
                description.usageCount(),
                description.totalAmount(), description.lastTransactionDate(), description.exampleDescription(), description.newInLatestStatement(),
                description.suggestion() == null ? null : new MerchantResponse(description.suggestion().merchantId(), description.suggestion().displayName(), description.suggestion().categoryName())
        );
    }
}
