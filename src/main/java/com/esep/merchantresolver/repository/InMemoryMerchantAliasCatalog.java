package com.esep.merchantresolver.repository;

import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.model.MerchantAliasRecord;

import java.util.List;
import java.util.Optional;

/**
 * Временный in-memory каталог алиасов известных продавцов.
 */
public class InMemoryMerchantAliasCatalog implements MerchantAliasCatalog {

    private static final List<MerchantAliasRecord> ALIASES = List.of(
            new MerchantAliasRecord("alias-001", "merchant-001", "MAGNUM", "MAGNUM", true),
            new MerchantAliasRecord("alias-002", "merchant-001", "MAGNUM CASH&CARRY", "MAGNUM CASH&CARRY", true),
            new MerchantAliasRecord("alias-003", "merchant-001", "MAGNUM CC", "MAGNUM CC", true),
            new MerchantAliasRecord("alias-004", "merchant-001", "TOO MAGNUM", "TOO MAGNUM", true),
            new MerchantAliasRecord("alias-005", "merchant-001", "ТОО MAGNUM", "ТОО MAGNUM", true),
            new MerchantAliasRecord("alias-006", "merchant-003", "YANDEX.GO", "YANDEX.GO", true),
            new MerchantAliasRecord("alias-007", "merchant-003", "YANDEX GO", "YANDEX GO", true)
    );

    @Override
    public Optional<MerchantAliasRecord> findByNormalizedAlias(String normalizedAlias) {
        return ALIASES.stream()
                .filter(alias -> alias.normalizedAlias().equals(normalizedAlias))
                .findFirst();
    }
}
