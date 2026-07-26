package com.esep.merchantresolver.model;

/**
 * Неизменяемый результат поиска продавца.
 */
public record MerchantMatch(
        String merchantId,
        String displayName,
        double confidence,
        boolean matched,
        boolean exactMatch
) {

    public static MerchantMatch matched(
            String merchantId,
            String displayName,
            double confidence,
            boolean exactMatch
    ) {
        return new MerchantMatch(merchantId, displayName, confidence, true, exactMatch);
    }

    public static MerchantMatch notMatched() {
        return new MerchantMatch(null, null, 0.0, false, false);
    }
}
