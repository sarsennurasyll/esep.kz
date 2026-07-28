package com.esep.merchantmanagement.api.dto;

import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.model.KnowledgeBaseMerchant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record KnowledgeBaseMerchantResponse(
        String id, String name, String normalizedName, MerchantType merchantType,
        String categoryCode, String categoryName, long transactionCount, BigDecimal totalAmount,
        LocalDate firstTransactionDate, LocalDate lastTransactionDate, List<KnowledgeBaseAliasResponse> aliases
) {
    public static KnowledgeBaseMerchantResponse from(KnowledgeBaseMerchant merchant) {
        return new KnowledgeBaseMerchantResponse(merchant.merchantReference().value(), merchant.name(), merchant.normalizedName(),
                merchant.merchantType(), merchant.categoryCode(), merchant.categoryName(), merchant.transactionCount(),
                merchant.totalAmount(), merchant.firstTransactionDate(), merchant.lastTransactionDate(),
                merchant.aliases().stream().map(KnowledgeBaseAliasResponse::from).toList());
    }
}
