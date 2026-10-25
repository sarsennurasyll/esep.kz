package com.esep.statementimport.exception;

/**
 * Сигнализирует о выписке, содержимое которой не соответствует ожидаемому формату банка.
 * Сообщение не должно содержать данных операций: оно может попасть в HTTP-ответ и логи.
 */
public class StatementFormatException extends RuntimeException {

    public StatementFormatException(String message) {
        super(message);
    }
}
