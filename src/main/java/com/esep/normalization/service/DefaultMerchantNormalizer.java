package com.esep.normalization.service;

import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.normalization.model.NormalizedMerchant;
import com.esep.normalization.rules.NormalizationRule;
import com.esep.normalization.rules.NormalizeSpacesRule;
import com.esep.normalization.rules.RemoveBranchNumberRule;
import com.esep.normalization.rules.RemoveLegalPrefixRule;
import com.esep.normalization.rules.RemoveLocationSuffixRule;
import com.esep.normalization.rules.RemoveSpecialCharactersRule;
import com.esep.normalization.rules.TrimRule;
import com.esep.normalization.rules.UpperCaseRule;

import java.math.BigDecimal;
import java.util.List;

/**
 * Базовая заготовка нормализатора названий продавцов.
 */
public class DefaultMerchantNormalizer implements MerchantNormalizer {

    private static final List<NormalizationRule> RULES = List.of(
            new TrimRule(),
            new UpperCaseRule(),
            new NormalizeSpacesRule(),
            new RemoveSpecialCharactersRule(),
            new RemoveLegalPrefixRule(),
            new RemoveLocationSuffixRule(),
            new RemoveBranchNumberRule()
    );

    @Override
    public NormalizedMerchant normalize(String merchantName) {
        String normalizedName = merchantName;

        for (NormalizationRule rule : RULES) {
            normalizedName = rule.apply(normalizedName);
        }

        BigDecimal confidence = normalizedName.isEmpty() ? BigDecimal.ZERO : BigDecimal.ONE;
        return new NormalizedMerchant(merchantName, normalizedName, confidence);
    }
}
