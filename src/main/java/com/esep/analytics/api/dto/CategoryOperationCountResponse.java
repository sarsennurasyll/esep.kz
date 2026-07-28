package com.esep.analytics.api.dto;

import com.esep.analytics.model.CategoryOperationCount;

/**
 * HTTP-представление количества операций по категории.
 */
public record CategoryOperationCountResponse(
        String category,
        String categoryName,
        long transactionCount
) {

    public static CategoryOperationCountResponse from(CategoryOperationCount count) {
        return new CategoryOperationCountResponse(count.category(), count.categoryName(), count.transactionCount());
    }
}
