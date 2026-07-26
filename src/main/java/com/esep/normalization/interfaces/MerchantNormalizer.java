package com.esep.normalization.interfaces;

import com.esep.normalization.model.NormalizedMerchant;

/**
 * Контракт для приведения названия продавца к единому виду.
 */
public interface MerchantNormalizer {

    NormalizedMerchant normalize(String merchantName);
}
