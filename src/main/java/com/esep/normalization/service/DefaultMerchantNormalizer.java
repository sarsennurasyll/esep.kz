package com.esep.normalization.service;

import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.normalization.model.NormalizedMerchant;
import org.springframework.stereotype.Component;

/**
 * Базовая заготовка нормализатора названий продавцов.
 */
@Component
public class DefaultMerchantNormalizer implements MerchantNormalizer {

    @Override
    public NormalizedMerchant normalize(String merchantName) {
        // TODO: Реализовать стандартную нормализацию названия продавца.
        throw new UnsupportedOperationException("Нормализация пока не реализована.");
    }
}
