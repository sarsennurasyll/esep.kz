package com.esep.statementimport.exception;

/**
 * Сигнализирует о повторной попытке импорта уже сохранённой выписки.
 */
public class StatementAlreadyImportedException extends RuntimeException {

    public StatementAlreadyImportedException(String sourceFileHash) {
        super("Statement has already been imported: " + sourceFileHash);
    }
}
