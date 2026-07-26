package com.esep.merchantresolver.interfaces;

import com.esep.merchantresolver.model.MerchantMatch;

/**
 * Контракт для поиска продавца по нормализованному названию.
 */
public interface MerchantResolver {

    MerchantMatch resolve(String normalizedMerchant);
}
