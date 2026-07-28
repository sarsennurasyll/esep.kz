package com.esep.merchantmanagement.api.dto;

import com.esep.entity.Category;

public record CategoryOptionResponse(String code, String name) {
    public static CategoryOptionResponse from(Category category) {
        return new CategoryOptionResponse(category.getCode(), category.getName());
    }
}
