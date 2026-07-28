package com.esep.merchantmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KnowledgeBaseAliasRequest(@NotBlank @Size(max = 255) String aliasName) {
}
