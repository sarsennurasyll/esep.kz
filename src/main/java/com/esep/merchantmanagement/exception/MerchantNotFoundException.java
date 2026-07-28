package com.esep.merchantmanagement.exception;

import com.esep.merchantresolver.model.MerchantReference;

/**
 * Продавец для создания алиаса не найден.
 */
public class MerchantNotFoundException extends RuntimeException {

    public MerchantNotFoundException(MerchantReference merchantReference) {
        super("Merchant was not found for reference: " + merchantReference.value());
    }
}
