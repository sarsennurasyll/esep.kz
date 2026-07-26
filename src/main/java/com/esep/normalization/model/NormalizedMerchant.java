package com.esep.normalization.model;

import java.math.BigDecimal;

/**
 * Результат нормализации названия продавца.
 */
public record NormalizedMerchant(
        String originalName,
        String normalizedName,
        BigDecimal confidence
) {
}
