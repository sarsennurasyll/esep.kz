package com.esep.statementimport.interfaces;

import com.esep.entity.BankType;
import com.esep.statementimport.model.ParsedStatement;

import java.io.InputStream;

/**
 * Целевой контракт парсера файла банковской выписки.
 */
public interface StatementParser {

    BankType supportedBank();

    ParsedStatement parse(InputStream input);
}
