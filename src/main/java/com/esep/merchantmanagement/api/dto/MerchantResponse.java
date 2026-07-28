package com.esep.merchantmanagement.api.dto;

import com.esep.merchantmanagement.model.MerchantSummary;

/**
 * HTTP-представление продавца для выбора соответствия.
 */
public record MerchantResponse(
        String id,
        String displayName
) {

    public static MerchantResponse from(MerchantSummary merchant) {
        return new MerchantResponse(merchant.merchantReference().value(), merchant.displayName());
    }
}
