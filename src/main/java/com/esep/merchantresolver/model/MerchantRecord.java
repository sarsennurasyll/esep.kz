package com.esep.merchantresolver.model;

/**
 * Неизменяемая запись известного продавца.
 */
public record MerchantRecord(
        MerchantReference merchantReference,
        String canonicalName
) {
}
