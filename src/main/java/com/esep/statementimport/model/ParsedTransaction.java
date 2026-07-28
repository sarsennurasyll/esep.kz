package com.esep.statementimport.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Неизменяемая операция, полученная после разбора банковской выписки.
 * Позиция записи используется только как детерминированный fallback для fingerprint.
 */
public record ParsedTransaction(
        LocalDate date,
        String description,
        BigDecimal amount,
        String currency,
        int sourceRecordPosition
) {

    public ParsedTransaction {
        if (sourceRecordPosition < 0) {
            throw new IllegalArgumentException("Позиция исходной записи не может быть отрицательной.");
        }
    }

    public ParsedTransaction(LocalDate date, String description, BigDecimal amount, String currency) {
        this(date, description, amount, currency, 0);
    }
}
