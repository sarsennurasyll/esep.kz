package com.esep.merchantmanagement.model;

import com.esep.entity.MerchantType;

public record KnowledgeBaseMerchantCommand(String name, MerchantType merchantType, String categoryCode) {
}
