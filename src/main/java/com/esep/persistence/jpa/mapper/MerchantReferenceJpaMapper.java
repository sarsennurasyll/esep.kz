package com.esep.persistence.jpa.mapper;

import com.esep.merchantresolver.model.MerchantReference;
import org.springframework.stereotype.Component;

/**
 * Преобразует непрозрачную прикладную ссылку в JPA-идентификатор продавца.
 */
@Component
public class MerchantReferenceJpaMapper {

    public MerchantReference toReference(Long merchantId) {
        return new MerchantReference(merchantId.toString());
    }

    public Long toMerchantId(MerchantReference merchantReference) {
        try {
            return Long.parseLong(merchantReference.value());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Merchant reference is not compatible with the JPA adapter", exception);
        }
    }
}
