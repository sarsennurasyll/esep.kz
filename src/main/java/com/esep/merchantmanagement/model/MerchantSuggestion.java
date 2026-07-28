package com.esep.merchantmanagement.model;

/** Рекомендация для ручного подтверждения, не влияющая на распознавание автоматически. */
public record MerchantSuggestion(String merchantId, String displayName, String categoryName) {
}
