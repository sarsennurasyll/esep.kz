package com.esep.merchantresolver.interfaces;

import com.esep.merchantresolver.model.MerchantAliasRecord;

import java.util.Optional;

/**
 * Контракт доступа к вариантам названий известных продавцов.
 */
public interface MerchantAliasCatalog {

    Optional<MerchantAliasRecord> findByNormalizedAlias(String normalizedAlias);
}
