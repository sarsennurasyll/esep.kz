package com.esep.parser.common;

/**
 * Сигнализирует об отсутствии парсера для указанного банка.
 */
public class UnsupportedBankException extends RuntimeException {

    public UnsupportedBankException(String message) {
        super(message);
    }
}
