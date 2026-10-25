package com.esep.moneyflow.model;

import java.math.BigDecimal;

/**
 * Насколько размечены операции, назначение которых знает только пользователь (метрика E3).
 *
 * @param labeledShare доля суммы с известным назначением, от 0 до 1
 */
public record LabelingProgress(
        BigDecimal totalAmount,
        BigDecimal labeledAmount,
        BigDecimal labeledShare,
        int counterparties,
        int rules
) {
}
