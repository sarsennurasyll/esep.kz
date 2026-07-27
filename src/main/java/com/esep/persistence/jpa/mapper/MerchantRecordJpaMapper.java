package com.esep.persistence.jpa.mapper;

import com.esep.entity.Merchant;
import com.esep.merchantresolver.model.MerchantRecord;
import org.springframework.stereotype.Component;

/**
 * Преобразует JPA-сущность продавца в прикладную запись каталога.
 */
@Component
public class MerchantRecordJpaMapper {

    private final MerchantReferenceJpaMapper merchantReferenceJpaMapper;

    public MerchantRecordJpaMapper(MerchantReferenceJpaMapper merchantReferenceJpaMapper) {
        this.merchantReferenceJpaMapper = merchantReferenceJpaMapper;
    }

    public MerchantRecord toRecord(Merchant merchant) {
        return new MerchantRecord(
                merchantReferenceJpaMapper.toReference(merchant.getId()),
                merchant.getNormalizedName()
        );
    }
}
