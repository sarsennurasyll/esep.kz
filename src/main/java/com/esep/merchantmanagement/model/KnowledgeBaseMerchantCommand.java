package com.esep.merchantmanagement.model;

import com.esep.entity.MerchantType;

/**
 * @param expectedVersion версия карточки, которую видел пользователь; {@code null} — без проверки
 */
public record KnowledgeBaseMerchantCommand(String name, MerchantType merchantType, String categoryCode, Long expectedVersion) {

    public KnowledgeBaseMerchantCommand(String name, MerchantType merchantType, String categoryCode) {
        this(name, merchantType, categoryCode, null);
    }
}
