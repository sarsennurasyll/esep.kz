package com.esep.analytics.api.dto;

import com.esep.analytics.model.PersonTransfer;

import java.math.BigDecimal;

/**
 * HTTP-представление исходящих переводов человеку.
 */
public record PersonTransferResponse(
        String recipient,
        BigDecimal amount,
        long transactionCount
) {

    public static PersonTransferResponse from(PersonTransfer transfer) {
        return new PersonTransferResponse(
                transfer.recipient(),
                transfer.amount(),
                transfer.transactionCount()
        );
    }
}
