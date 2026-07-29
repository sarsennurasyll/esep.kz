package com.esep.statementimport.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.esep.entity.BankOperationType;

/**
 * Неизменяемая операция, полученная после разбора банковской выписки.
 * Позиция записи используется только как детерминированный fallback для fingerprint.
 */
public record ParsedTransaction(
        LocalDate date,
        String description,
        BigDecimal amount,
        String currency,
        BankOperationType bankOperationType,
        int sourceRecordPosition
) {

    public ParsedTransaction {
        if (sourceRecordPosition < 0) {
            throw new IllegalArgumentException("Позиция исходной записи не может быть отрицательной.");
        }
    }

    public ParsedTransaction(LocalDate date, String description, BigDecimal amount, String currency) {
        this(date, description, amount, currency, BankOperationType.PURCHASE, 0);
    }

    public ParsedTransaction(LocalDate date, String description, BigDecimal amount, String currency, int sourceRecordPosition) {
        this(date, description, amount, currency, BankOperationType.PURCHASE, sourceRecordPosition);
    }
}
