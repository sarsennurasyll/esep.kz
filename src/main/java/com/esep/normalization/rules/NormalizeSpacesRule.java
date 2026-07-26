package com.esep.normalization.rules;

import com.esep.normalization.util.MerchantTextUtils;

/**
 * Заменяет повторяющиеся пробелы одним пробелом.
 */
public final class NormalizeSpacesRule implements NormalizationRule {

    @Override
    public String apply(String value) {
        return MerchantTextUtils.normalizeSpaces(value);
    }
}
