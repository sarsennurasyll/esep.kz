package com.esep.normalization.rules;

import com.esep.normalization.util.MerchantTextUtils;

/**
 * Удаляет юридический префикс в начале названия продавца.
 */
public final class RemoveLegalPrefixRule implements NormalizationRule {

    @Override
    public String apply(String value) {
        return MerchantTextUtils.removeLegalEntityPrefix(value);
    }
}
