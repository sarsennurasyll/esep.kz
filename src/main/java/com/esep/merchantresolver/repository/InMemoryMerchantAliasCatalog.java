package com.esep.merchantresolver.repository;

import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantmanagement.interfaces.MerchantAliasMatchCatalog;
import com.esep.merchantmanagement.model.MerchantAliasMatchCommand;
import com.esep.merchantresolver.model.MerchantAliasRecord;
import com.esep.merchantresolver.model.MerchantReference;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Временный in-memory каталог алиасов известных продавцов.
 */
public class InMemoryMerchantAliasCatalog implements MerchantAliasCatalog, MerchantAliasMatchCatalog {

    private final CopyOnWriteArrayList<MerchantAliasRecord> aliases = new CopyOnWriteArrayList<>(java.util.List.of(
            new MerchantAliasRecord("alias-001", new MerchantReference("merchant-001"), "MAGNUM", "MAGNUM", true),
            new MerchantAliasRecord("alias-002", new MerchantReference("merchant-001"), "MAGNUM CASH&CARRY", "MAGNUM CASH&CARRY", true),
            new MerchantAliasRecord("alias-003", new MerchantReference("merchant-001"), "MAGNUM CC", "MAGNUM CC", true),
            new MerchantAliasRecord("alias-004", new MerchantReference("merchant-001"), "TOO MAGNUM", "TOO MAGNUM", true),
            new MerchantAliasRecord("alias-005", new MerchantReference("merchant-001"), "ТОО MAGNUM", "ТОО MAGNUM", true),
            new MerchantAliasRecord("alias-006", new MerchantReference("merchant-003"), "YANDEX.GO", "YANDEX.GO", true),
            new MerchantAliasRecord("alias-007", new MerchantReference("merchant-003"), "YANDEX GO", "YANDEX GO", true)
    ));

    @Override
    public Optional<MerchantAliasRecord> findByNormalizedAlias(String normalizedAlias) {
        return aliases.stream()
                .filter(alias -> alias.normalizedAlias().equals(normalizedAlias))
                .findFirst();
    }

    @Override
    public void save(MerchantAliasMatchCommand command) {
        aliases.add(new MerchantAliasRecord(
                java.util.UUID.randomUUID().toString(),
                command.merchantReference(),
                command.aliasName(),
                command.normalizedAlias(),
                true
        ));
    }
}
