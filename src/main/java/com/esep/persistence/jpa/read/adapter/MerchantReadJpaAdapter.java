package com.esep.persistence.jpa.read.adapter;

import com.esep.merchantmanagement.interfaces.MerchantReadQuery;
import com.esep.merchantmanagement.model.MerchantSummary;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import com.esep.persistence.jpa.read.repository.MerchantReadJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-адаптер read-порта списка продавцов.
 */
@Repository
public class MerchantReadJpaAdapter implements MerchantReadQuery {

    private final MerchantReadJpaRepository merchantReadJpaRepository;
    private final MerchantReferenceJpaMapper merchantReferenceJpaMapper;

    public MerchantReadJpaAdapter(
            MerchantReadJpaRepository merchantReadJpaRepository,
            MerchantReferenceJpaMapper merchantReferenceJpaMapper
    ) {
        this.merchantReadJpaRepository = merchantReadJpaRepository;
        this.merchantReferenceJpaMapper = merchantReferenceJpaMapper;
    }

    @Override
    public List<MerchantSummary> findAll() {
        return merchantReadJpaRepository.findAllForSelection().stream()
                .map(merchant -> new MerchantSummary(
                        merchantReferenceJpaMapper.toReference(merchant.getId()),
                        merchant.getOriginalName()
                ))
                .toList();
    }
}
