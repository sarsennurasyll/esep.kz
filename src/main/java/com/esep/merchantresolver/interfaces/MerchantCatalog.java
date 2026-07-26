package com.esep.merchantresolver.interfaces;

import com.esep.merchantresolver.model.MerchantRecord;

import java.util.Optional;

/**
 * Контракт доступа к известным продавцам и их вариантам названий.
 */
public interface MerchantCatalog {

    Optional<MerchantRecord> findByCanonicalName(String normalizedMerchant);

    Optional<MerchantRecord> findByAlias(String normalizedMerchant);
}
