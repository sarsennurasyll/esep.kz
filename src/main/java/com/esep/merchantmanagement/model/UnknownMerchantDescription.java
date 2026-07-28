package com.esep.merchantmanagement.model;

/**
 * Нормализованное неизвестное описание для подтверждения пользователем.
 */
public record UnknownMerchantDescription(
        String normalizedDescription,
        long usageCount,
        String exampleDescription
) {
}
