package com.esep.statementimport.api.dto;

import com.esep.statementimport.model.StatementDeletionPlan;

import java.util.List;

/**
 * Выписки, удаляемые вместе с выбранной, и число их операций.
 */
public record StatementDeletionResponse(long statementId, List<Long> statementIds, long operationCount) {

    public static StatementDeletionResponse from(StatementDeletionPlan plan) {
        return new StatementDeletionResponse(plan.statementId(), plan.statementIds(), plan.operationCount());
    }
}
