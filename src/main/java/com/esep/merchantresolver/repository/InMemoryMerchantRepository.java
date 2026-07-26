package com.esep.merchantresolver.repository;

import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.model.MerchantRecord;

import java.util.List;
import java.util.Optional;

/**
 * Временный каталог известных продавцов, работающий в памяти приложения.
 */
public class InMemoryMerchantRepository implements MerchantCatalog {

    private static final List<MerchantRecord> MERCHANTS = List.of(
            new MerchantRecord("merchant-001", "MAGNUM"),
            new MerchantRecord("merchant-002", "EUROPHARMA"),
            new MerchantRecord("merchant-003", "YANDEX GO"),
            new MerchantRecord("merchant-004", "ONAY"),
            new MerchantRecord("merchant-005", "ACTIV"),
            new MerchantRecord("merchant-006", "ALTEL"),
            new MerchantRecord("merchant-007", "TELE2"),
            new MerchantRecord("merchant-008", "FLO"),
            new MerchantRecord("merchant-009", "SUPERCELL"),
            new MerchantRecord("merchant-010", "SATTI"),
            new MerchantRecord("merchant-011", "BRO JETISU")
    );

    @Override
    public Optional<MerchantRecord> findByCanonicalName(String normalizedMerchant) {
        return MERCHANTS.stream()
                .filter(merchant -> merchant.canonicalName().equals(normalizedMerchant))
                .findFirst();
    }

    @Override
    public Optional<MerchantRecord> findById(String merchantId) {
        return MERCHANTS.stream()
                .filter(merchant -> merchant.id().equals(merchantId))
                .findFirst();
    }
}
