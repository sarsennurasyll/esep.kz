package com.esep.persistence.jpa.adapter;

import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.model.MerchantAliasRecord;
import com.esep.persistence.jpa.mapper.MerchantAliasRecordJpaMapper;
import com.esep.persistence.jpa.repository.MerchantAliasJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA-адаптер существующего каталога алиасов продавцов.
 */
@Repository
public class MerchantAliasCatalogJpaAdapter implements MerchantAliasCatalog {

    private final MerchantAliasJpaRepository merchantAliasJpaRepository;
    private final MerchantAliasRecordJpaMapper merchantAliasRecordJpaMapper;

    public MerchantAliasCatalogJpaAdapter(
            MerchantAliasJpaRepository merchantAliasJpaRepository,
            MerchantAliasRecordJpaMapper merchantAliasRecordJpaMapper
    ) {
        this.merchantAliasJpaRepository = merchantAliasJpaRepository;
        this.merchantAliasRecordJpaMapper = merchantAliasRecordJpaMapper;
    }

    @Override
    public Optional<MerchantAliasRecord> findByNormalizedAlias(String normalizedAlias) {
        return merchantAliasJpaRepository.findByNormalizedAlias(normalizedAlias)
                .map(merchantAliasRecordJpaMapper::toRecord);
    }
}
