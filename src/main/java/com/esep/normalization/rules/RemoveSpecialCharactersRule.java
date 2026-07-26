package com.esep.normalization.rules;

import com.esep.normalization.util.MerchantTextUtils;

/**
 * Удаляет специальные символы на границах названия.
 */
public final class RemoveSpecialCharactersRule implements NormalizationRule {

    @Override
    public String apply(String value) {
        return MerchantTextUtils.removeSpecialCharacters(value);
    }
}
