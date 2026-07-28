package com.esep.merchantmanagement.exception;

/**
 * Алиас уже связан с продавцом.
 */
public class MerchantAliasAlreadyExistsException extends RuntimeException {

    public MerchantAliasAlreadyExistsException(String normalizedAlias) {
        super("Merchant alias already exists: " + normalizedAlias);
    }
}
