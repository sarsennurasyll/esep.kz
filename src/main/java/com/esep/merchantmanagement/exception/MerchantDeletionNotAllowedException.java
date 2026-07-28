package com.esep.merchantmanagement.exception;

import com.esep.merchantresolver.model.MerchantReference;

public class MerchantDeletionNotAllowedException extends RuntimeException {

    public MerchantDeletionNotAllowedException(MerchantReference merchantReference) {
        super("Merchant cannot be deleted while it is used by operations or aliases: " + merchantReference.value());
    }
}
