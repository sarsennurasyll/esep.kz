package com.esep.merchantmanagement.exception;

/** Карточку продавца изменили после того, как пользователь открыл её для правки. */
public class MerchantVersionConflictException extends RuntimeException {

    public MerchantVersionConflictException() {
        super("Merchant was changed by another request; reload it and repeat the change");
    }
}
