package com.esep.merchantresolver.service;

import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.interfaces.MerchantResolver;
import com.esep.merchantresolver.model.MerchantAliasRecord;
import com.esep.merchantresolver.model.MerchantMatch;
import com.esep.merchantresolver.model.MerchantRecord;

import java.util.Optional;

/**
 * Базовый resolver, выполняющий точный поиск продавца в каталоге.
 */
public class InMemoryMerchantResolver implements MerchantResolver {

    private final MerchantCatalog merchantCatalog;
    private final MerchantAliasCatalog merchantAliasCatalog;

    public InMemoryMerchantResolver(MerchantCatalog merchantCatalog, MerchantAliasCatalog merchantAliasCatalog) {
        this.merchantCatalog = merchantCatalog;
        this.merchantAliasCatalog = merchantAliasCatalog;
    }

    @Override
    public MerchantMatch resolve(String normalizedMerchant) {
        if (normalizedMerchant == null || normalizedMerchant.isBlank()) {
            return MerchantMatch.notMatched();
        }

        Optional<MerchantRecord> canonicalMatch = merchantCatalog.findByCanonicalName(normalizedMerchant);
        if (canonicalMatch.isPresent()) {
            return toExactMatch(canonicalMatch.get());
        }

        return merchantAliasCatalog.findByNormalizedAlias(normalizedMerchant)
                .map(MerchantAliasRecord::merchantReference)
                .flatMap(merchantCatalog::findByReference)
                .map(this::toExactMatch)
                .orElseGet(MerchantMatch::notMatched);
    }

    private MerchantMatch toExactMatch(MerchantRecord merchantRecord) {
        return MerchantMatch.matched(
                merchantRecord.merchantReference(),
                merchantRecord.canonicalName(),
                1.0,
                true
        );
    }
}
