package com.esep.merchantmanagement.model;

import com.esep.merchantresolver.model.MerchantReference;

/**
 * Краткое представление продавца для выбора в интерфейсе.
 */
public record MerchantSummary(
        MerchantReference merchantReference,
        String displayName,
        String categoryName,
        java.util.List<String> aliases
) {
    public MerchantSummary(MerchantReference merchantReference, String displayName, String categoryName) {
        this(merchantReference, displayName, categoryName, java.util.List.of());
    }
}
