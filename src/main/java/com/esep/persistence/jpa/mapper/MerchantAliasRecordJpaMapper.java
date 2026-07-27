package com.esep.persistence.jpa.mapper;

import com.esep.entity.MerchantAlias;
import com.esep.merchantresolver.model.MerchantAliasRecord;
import org.springframework.stereotype.Component;

/**
 * Преобразует JPA-сущность алиаса в прикладную запись каталога.
 */
@Component
public class MerchantAliasRecordJpaMapper {

    private final MerchantReferenceJpaMapper merchantReferenceJpaMapper;

    public MerchantAliasRecordJpaMapper(MerchantReferenceJpaMapper merchantReferenceJpaMapper) {
        this.merchantReferenceJpaMapper = merchantReferenceJpaMapper;
    }

    public MerchantAliasRecord toRecord(MerchantAlias merchantAlias) {
        return new MerchantAliasRecord(
                merchantAlias.getId().toString(),
                merchantReferenceJpaMapper.toReference(merchantAlias.getMerchant().getId()),
                merchantAlias.getAliasName(),
                merchantAlias.getNormalizedAlias(),
                merchantAlias.isVerified()
        );
    }
}
