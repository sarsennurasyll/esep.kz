package com.esep.merchantrecognition.model;

import com.esep.merchantresolver.model.MerchantMatch;

/**
 * Неизменяемый результат нормализации и распознавания продавца.
 */
public record MerchantRecognitionResult(
        String rawMerchant,
        String normalizedMerchant,
        MerchantMatch merchantMatch
) {
}
