package com.esep.merchantresolver.service;

import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.interfaces.MerchantResolver;
import com.esep.merchantresolver.model.MerchantMatch;
import com.esep.merchantresolver.model.MerchantRecord;

import java.util.Optional;

/**
 * Базовый resolver, выполняющий точный поиск продавца в каталоге.
 */
public class InMemoryMerchantResolver implements MerchantResolver {

    private final MerchantCatalog merchantCatalog;

    public InMemoryMerchantResolver(MerchantCatalog merchantCatalog) {
        this.merchantCatalog = merchantCatalog;
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

        return merchantCatalog.findByAlias(normalizedMerchant)
                .map(this::toExactMatch)
                .orElseGet(MerchantMatch::notMatched);
    }

    private MerchantMatch toExactMatch(MerchantRecord merchantRecord) {
        return MerchantMatch.matched(
                merchantRecord.id(),
                merchantRecord.canonicalName(),
                1.0,
                true
        );
    }
}
