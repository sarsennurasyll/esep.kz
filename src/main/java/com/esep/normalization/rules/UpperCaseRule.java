package com.esep.normalization.rules;

import com.esep.normalization.util.MerchantTextUtils;

/**
 * Приводит название продавца к верхнему регистру.
 */
public final class UpperCaseRule implements NormalizationRule {

    @Override
    public String apply(String value) {
        return MerchantTextUtils.toUpper(value);
    }
}
