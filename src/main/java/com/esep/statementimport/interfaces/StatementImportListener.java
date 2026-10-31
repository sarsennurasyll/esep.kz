package com.esep.statementimport.interfaces;

/**
 * Вызывается в транзакции импорта после сохранения операций.
 */
public interface StatementImportListener {

    void statementImported();
}
