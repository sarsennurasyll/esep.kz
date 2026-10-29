package com.esep.merchantmanagement.api.dto;

import com.esep.entity.MerchantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record KnowledgeBaseMerchantRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull MerchantType merchantType,
        @NotBlank @Size(max = 50) String categoryCode,
        @PositiveOrZero Long version
) {

    public KnowledgeBaseMerchantRequest(String name, MerchantType merchantType, String categoryCode) {
        this(name, merchantType, categoryCode, null);
    }
}
