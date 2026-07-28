package com.esep.persistence.jpa.read.adapter;

import com.esep.merchantmanagement.interfaces.UnknownMerchantDescriptionQuery;
import com.esep.merchantmanagement.model.UnknownMerchantCandidate;
import com.esep.persistence.jpa.read.repository.UnknownMerchantReadJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-адаптер read-порта неизвестных описаний операций.
 */
@Repository
public class UnknownMerchantDescriptionJpaAdapter implements UnknownMerchantDescriptionQuery {

    private final UnknownMerchantReadJpaRepository unknownMerchantReadJpaRepository;

    public UnknownMerchantDescriptionJpaAdapter(UnknownMerchantReadJpaRepository unknownMerchantReadJpaRepository) {
        this.unknownMerchantReadJpaRepository = unknownMerchantReadJpaRepository;
    }

    @Override
    public List<UnknownMerchantCandidate> findAll() {
        return unknownMerchantReadJpaRepository.findUnknownDescriptions().stream()
                .map(result -> new UnknownMerchantCandidate(result.getDescription(), result.getUsageCount()))
                .toList();
    }
}
