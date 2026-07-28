package com.esep.merchantmanagement.api.dto;

import com.esep.merchantmanagement.model.KnowledgeBaseAlias;

public record KnowledgeBaseAliasResponse(Long id, String aliasName, String normalizedAlias, boolean verified) {
    public static KnowledgeBaseAliasResponse from(KnowledgeBaseAlias alias) {
        return new KnowledgeBaseAliasResponse(alias.id(), alias.aliasName(), alias.normalizedAlias(), alias.verified());
    }
}
