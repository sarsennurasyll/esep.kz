package com.esep.moneyflow.exception;

/** Операция с указанным идентификатором не найдена. */
public class OperationNotFoundException extends RuntimeException {

    public OperationNotFoundException(long operationId) {
        super("Operation was not found: " + operationId);
    }
}
