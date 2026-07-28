package com.esep.merchantmanagement.model;

import com.esep.entity.MerchantType;
import com.esep.merchantresolver.model.MerchantReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record KnowledgeBaseMerchant(
        MerchantReference merchantReference,
        String name,
        String normalizedName,
        MerchantType merchantType,
        String categoryCode,
        String categoryName,
        long transactionCount,
        BigDecimal totalAmount,
        LocalDate firstTransactionDate,
        LocalDate lastTransactionDate,
        List<KnowledgeBaseAlias> aliases
) {
}
