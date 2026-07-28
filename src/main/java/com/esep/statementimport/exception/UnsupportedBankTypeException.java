package com.esep.statementimport.exception;

import com.esep.entity.BankType;

/**
 * Для выбранного банка не зарегистрирован parser.
 */
public class UnsupportedBankTypeException extends IllegalArgumentException {

    public UnsupportedBankTypeException(BankType bankType) {
        super("Statement parser is not supported for bank: " + bankType);
    }
}
