package com.esep.merchantresolver.model;

import java.util.Objects;

/**
 * Непрозрачная прикладная ссылка на продавца.
 */
public record MerchantReference(String value) {

    public MerchantReference {
        Objects.requireNonNull(value, "Merchant reference must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("Merchant reference must not be blank");
        }
    }
}
