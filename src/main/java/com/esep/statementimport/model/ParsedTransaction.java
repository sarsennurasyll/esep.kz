package com.esep.statementimport.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Неизменяемая операция, полученная после разбора банковской выписки.
 */
public record ParsedTransaction(
        LocalDate date,
        String description,
        BigDecimal amount,
        String currency
) {
}
