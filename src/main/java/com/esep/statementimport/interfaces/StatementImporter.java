package com.esep.statementimport.interfaces;

import com.esep.statementimport.model.ImportResult;
import com.esep.statementimport.model.ParsedStatement;

/**
 * Контракт импорта уже распарсенной банковской выписки.
 */
public interface StatementImporter {

    ImportResult importStatement(ParsedStatement statement);
}
