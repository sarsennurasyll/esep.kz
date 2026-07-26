package com.esep.normalization.service;

import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.normalization.model.NormalizedMerchant;
import com.esep.normalization.util.MerchantTextUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Базовая заготовка нормализатора названий продавцов.
 */
@Component
public class DefaultMerchantNormalizer implements MerchantNormalizer {

    @Override
    public NormalizedMerchant normalize(String merchantName) {
        String normalizedName = MerchantTextUtils.trim(merchantName);
        normalizedName = MerchantTextUtils.toUpper(normalizedName);
        normalizedName = MerchantTextUtils.normalizeSpaces(normalizedName);
        normalizedName = MerchantTextUtils.removeSpecialCharacters(normalizedName);

        BigDecimal confidence = normalizedName.isEmpty() ? BigDecimal.ZERO : BigDecimal.ONE;
        return new NormalizedMerchant(merchantName, normalizedName, confidence);
    }
}
