package com.esep.statementimport.model;

import java.util.List;

/**
 * Что будет удалено вместе с выпиской.
 *
 * @param statementIds выбранная выписка и выписки, импортированные после неё с пересекающимся периодом того же счёта
 * @param operationCount число операций во всех этих выписках
 */
public record StatementDeletionPlan(long statementId, List<Long> statementIds, long operationCount) {

    public StatementDeletionPlan {
        statementIds = List.copyOf(statementIds);
    }

    public boolean includesOtherStatements() {
        return statementIds.size() > 1;
    }
}
