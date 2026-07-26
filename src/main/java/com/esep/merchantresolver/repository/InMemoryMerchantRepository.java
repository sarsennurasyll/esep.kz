package com.esep.merchantresolver.repository;

import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.model.MerchantRecord;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Временный каталог известных продавцов, работающий в памяти приложения.
 */
public class InMemoryMerchantRepository implements MerchantCatalog {

    private static final List<MerchantRecord> MERCHANTS = List.of(
            new MerchantRecord("merchant-001", "MAGNUM", Set.of("MAGNUM", "MAGNUM CASH&CARRY", "MAGNUM CC")),
            new MerchantRecord("merchant-002", "EUROPHARMA", Set.of("EUROPHARMA")),
            new MerchantRecord("merchant-003", "YANDEX GO", Set.of("YANDEX GO", "YANDEXGO")),
            new MerchantRecord("merchant-004", "ONAY", Set.of("ONAY")),
            new MerchantRecord("merchant-005", "ACTIV", Set.of("ACTIV")),
            new MerchantRecord("merchant-006", "ALTEL", Set.of("ALTEL")),
            new MerchantRecord("merchant-007", "TELE2", Set.of("TELE2")),
            new MerchantRecord("merchant-008", "FLO", Set.of("FLO")),
            new MerchantRecord("merchant-009", "SUPERCELL", Set.of("SUPERCELL")),
            new MerchantRecord("merchant-010", "SATTI", Set.of("SATTI")),
            new MerchantRecord("merchant-011", "BRO JETISU", Set.of("BRO JETISU"))
    );

    @Override
    public Optional<MerchantRecord> findByCanonicalName(String normalizedMerchant) {
        return MERCHANTS.stream()
                .filter(merchant -> merchant.canonicalName().equals(normalizedMerchant))
                .findFirst();
    }

    @Override
    public Optional<MerchantRecord> findByAlias(String normalizedMerchant) {
        return MERCHANTS.stream()
                .filter(merchant -> merchant.aliases().contains(normalizedMerchant))
                .findFirst();
    }
}
