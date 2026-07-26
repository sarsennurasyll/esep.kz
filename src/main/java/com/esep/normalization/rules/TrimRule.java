package com.esep.normalization.rules;

import com.esep.normalization.util.MerchantTextUtils;

/**
 * Удаляет пробельные символы в начале и конце названия.
 */
public final class TrimRule implements NormalizationRule {

    @Override
    public String apply(String value) {
        return MerchantTextUtils.trim(value);
    }
}
