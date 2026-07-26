package com.esep.merchantresolver.model;

/**
 * Неизменяемая запись известного продавца.
 */
public record MerchantRecord(
        String id,
        String canonicalName
) {
}
