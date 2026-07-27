package com.esep.statementimport.exception;

/**
 * Сигнализирует о выписке без операций, из которой нельзя определить период.
 */
public class StatementWithoutTransactionsException extends RuntimeException {

    public StatementWithoutTransactionsException() {
        super("Statement does not contain transactions");
    }
}
