package com.esep.merchantmanagement.api.dto;

import com.esep.merchantmanagement.model.UnknownMerchantDescription;

/**
 * HTTP-представление неизвестного описания операции.
 */
public record UnknownMerchantResponse(
        String normalizedDescription,
        long usageCount,
        String exampleDescription
) {

    public static UnknownMerchantResponse from(UnknownMerchantDescription description) {
        return new UnknownMerchantResponse(
                description.normalizedDescription(),
                description.usageCount(),
                description.exampleDescription()
        );
    }
}
