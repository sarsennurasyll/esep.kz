package com.esep.persistence.jpa.adapter;

import com.esep.entity.Merchant;
import com.esep.entity.MerchantAlias;
import com.esep.merchantmanagement.interfaces.MerchantAliasMatchCatalog;
import com.esep.merchantmanagement.model.MerchantAliasMatchCommand;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import com.esep.persistence.jpa.repository.MerchantAliasJpaRepository;
import com.esep.persistence.jpa.repository.MerchantJpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-адаптер command-порта сохранения подтверждённых алиасов.
 */
@Repository
public class MerchantAliasMatchCatalogJpaAdapter implements MerchantAliasMatchCatalog {

    private final MerchantAliasJpaRepository merchantAliasJpaRepository;
    private final MerchantJpaRepository merchantJpaRepository;
    private final MerchantReferenceJpaMapper merchantReferenceJpaMapper;

    public MerchantAliasMatchCatalogJpaAdapter(
            MerchantAliasJpaRepository merchantAliasJpaRepository,
            MerchantJpaRepository merchantJpaRepository,
            MerchantReferenceJpaMapper merchantReferenceJpaMapper
    ) {
        this.merchantAliasJpaRepository = merchantAliasJpaRepository;
        this.merchantJpaRepository = merchantJpaRepository;
        this.merchantReferenceJpaMapper = merchantReferenceJpaMapper;
    }

    @Override
    public void save(MerchantAliasMatchCommand command) {
        Merchant merchant = merchantJpaRepository.findById(
                        merchantReferenceJpaMapper.toMerchantId(command.merchantReference())
                )
                .orElseThrow(() -> new IllegalArgumentException("Merchant was not found for the reference"));

        merchantAliasJpaRepository.save(MerchantAlias.builder()
                .aliasName(command.aliasName())
                .normalizedAlias(command.normalizedAlias())
                .merchant(merchant)
                .verified(true)
                .build());
    }
}
