package com.esep.merchantresolver.repository;

import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.model.MerchantRecord;
import com.esep.merchantresolver.model.MerchantReference;

import java.util.List;
import java.util.Optional;

/**
 * Временный каталог известных продавцов, работающий в памяти приложения.
 */
public class InMemoryMerchantRepository implements MerchantCatalog {

    private static final List<MerchantRecord> MERCHANTS = List.of(
            new MerchantRecord(new MerchantReference("merchant-001"), "MAGNUM"),
            new MerchantRecord(new MerchantReference("merchant-002"), "EUROPHARMA"),
            new MerchantRecord(new MerchantReference("merchant-003"), "YANDEX GO"),
            new MerchantRecord(new MerchantReference("merchant-004"), "ONAY"),
            new MerchantRecord(new MerchantReference("merchant-005"), "ACTIV"),
            new MerchantRecord(new MerchantReference("merchant-006"), "ALTEL"),
            new MerchantRecord(new MerchantReference("merchant-007"), "TELE2"),
            new MerchantRecord(new MerchantReference("merchant-008"), "FLO"),
            new MerchantRecord(new MerchantReference("merchant-009"), "SUPERCELL"),
            new MerchantRecord(new MerchantReference("merchant-010"), "SATTI"),
            new MerchantRecord(new MerchantReference("merchant-011"), "BRO JETISU")
    );

    @Override
    public Optional<MerchantRecord> findByCanonicalName(String normalizedMerchant) {
        return MERCHANTS.stream()
                .filter(merchant -> merchant.canonicalName().equals(normalizedMerchant))
                .findFirst();
    }

    @Override
    public Optional<MerchantRecord> findByReference(MerchantReference merchantReference) {
        return MERCHANTS.stream()
                .filter(merchant -> merchant.merchantReference().equals(merchantReference))
                .findFirst();
    }
}
