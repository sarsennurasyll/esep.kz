package com.esep.merchantmanagement.model;

/**
 * Сырой агрегат неизвестного описания из хранилища операций.
 */
public record UnknownMerchantCandidate(
        String description,
        long usageCount
) {
}
