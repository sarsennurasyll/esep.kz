package com.esep.merchantresolver.model;

/**
 * Временная in-memory запись варианта названия продавца для поиска.
 */
public record MerchantAliasRecord(
        String id,
        String merchantId,
        String alias,
        String normalizedAlias,
        boolean verified
) {
}
