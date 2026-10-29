package com.esep.statementimport.exception;

import com.esep.statementimport.model.StatementDeletionPlan;

/** Подтверждённый набор выписок не совпадает с тем, что нужно удалить вместе с выбранной. */
public class StatementDeletionMismatchException extends RuntimeException {

    private final transient StatementDeletionPlan plan;

    public StatementDeletionMismatchException(StatementDeletionPlan plan) {
        super("Statements to delete must be confirmed together with dependent overlapping statements");
        this.plan = plan;
    }

    public StatementDeletionPlan plan() {
        return plan;
    }
}
