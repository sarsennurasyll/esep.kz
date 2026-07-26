package com.esep.normalization.rules;

import com.esep.normalization.util.MerchantTextUtils;

/**
 * Удаляет известный географический суффикс в конце названия.
 */
public final class RemoveLocationSuffixRule implements NormalizationRule {

    @Override
    public String apply(String value) {
        return MerchantTextUtils.removeLocationSuffix(value);
    }
}
