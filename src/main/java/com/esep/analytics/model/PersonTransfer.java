package com.esep.analytics.model;

import java.math.BigDecimal;

/**
 * Агрегированные исходящие переводы физическому лицу.
 */
public record PersonTransfer(
        String recipient,
        BigDecimal amount,
        long transactionCount
) {
}
