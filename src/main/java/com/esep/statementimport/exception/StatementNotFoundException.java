package com.esep.statementimport.exception;

public class StatementNotFoundException extends RuntimeException {

    public StatementNotFoundException(long statementId) {
        super("Statement " + statementId + " was not found");
    }
}
