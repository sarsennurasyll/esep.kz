package com.esep.parser.common;

/**
 * Сигнализирует о неподдерживаемом формате файла выписки.
 */
public class UnsupportedFileTypeException extends RuntimeException {

    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
