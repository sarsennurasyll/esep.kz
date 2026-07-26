package com.esep.merchantrecognition.service;

import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantrecognition.model.MerchantRecognitionResult;
import com.esep.merchantresolver.interfaces.MerchantResolver;
import com.esep.merchantresolver.model.MerchantMatch;
import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.normalization.model.NormalizedMerchant;

/**
 * Оркеструет нормализацию и точное распознавание продавца.
 */
public class DefaultMerchantRecognitionService implements MerchantRecognitionService {

    private final MerchantNormalizer merchantNormalizer;
    private final MerchantResolver merchantResolver;

    public DefaultMerchantRecognitionService(
            MerchantNormalizer merchantNormalizer,
            MerchantResolver merchantResolver
    ) {
        this.merchantNormalizer = merchantNormalizer;
        this.merchantResolver = merchantResolver;
    }

    @Override
    public MerchantRecognitionResult recognize(String rawMerchant) {
        if (rawMerchant == null || rawMerchant.isBlank()) {
            return new MerchantRecognitionResult(rawMerchant, "", MerchantMatch.notMatched());
        }

        NormalizedMerchant normalizedMerchant = merchantNormalizer.normalize(rawMerchant);
        MerchantMatch merchantMatch = merchantResolver.resolve(normalizedMerchant.normalizedName());

        return new MerchantRecognitionResult(
                rawMerchant,
                normalizedMerchant.normalizedName(),
                merchantMatch
        );
    }
}
