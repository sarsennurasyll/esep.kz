package com.esep.normalization.rules;

import com.esep.normalization.util.MerchantTextUtils;

/**
 * Удаляет номер филиала в конце названия продавца.
 */
public final class RemoveBranchNumberRule implements NormalizationRule {

    @Override
    public String apply(String value) {
        return MerchantTextUtils.removeBranchNumber(value);
    }
}
