package com.esep.persistence.jpa.adapter;

import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.model.MerchantRecord;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.persistence.jpa.mapper.MerchantRecordJpaMapper;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import com.esep.persistence.jpa.repository.MerchantJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA-адаптер существующего каталога продавцов.
 */
@Repository
public class MerchantCatalogJpaAdapter implements MerchantCatalog {

    private final MerchantJpaRepository merchantJpaRepository;
    private final MerchantRecordJpaMapper merchantRecordJpaMapper;
    private final MerchantReferenceJpaMapper merchantReferenceJpaMapper;

    public MerchantCatalogJpaAdapter(
            MerchantJpaRepository merchantJpaRepository,
            MerchantRecordJpaMapper merchantRecordJpaMapper,
            MerchantReferenceJpaMapper merchantReferenceJpaMapper
    ) {
        this.merchantJpaRepository = merchantJpaRepository;
        this.merchantRecordJpaMapper = merchantRecordJpaMapper;
        this.merchantReferenceJpaMapper = merchantReferenceJpaMapper;
    }

    @Override
    public Optional<MerchantRecord> findByCanonicalName(String normalizedMerchant) {
        return merchantJpaRepository.findByNormalizedName(normalizedMerchant)
                .map(merchantRecordJpaMapper::toRecord);
    }

    @Override
    public Optional<MerchantRecord> findByReference(MerchantReference merchantReference) {
        return merchantJpaRepository.findById(merchantReferenceJpaMapper.toMerchantId(merchantReference))
                .map(merchantRecordJpaMapper::toRecord);
    }
}
