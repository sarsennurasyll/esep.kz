package com.esep.statementimport.interfaces;

import com.esep.entity.BankType;

/**
 * Контракт выбора parser по типу банка.
 */
public interface StatementParserRegistry {

    StatementParser getParser(BankType bankType);
}
