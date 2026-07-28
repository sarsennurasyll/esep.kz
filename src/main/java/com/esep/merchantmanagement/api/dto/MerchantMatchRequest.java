package com.esep.merchantmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на подтверждение соответствия описания и продавца.
 */
public record MerchantMatchRequest(
        @NotBlank String normalizedDescription,
        @NotBlank String merchantId
) {
}
