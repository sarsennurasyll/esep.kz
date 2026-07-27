package com.esep.persistence.model;

import com.esep.entity.BankType;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Входные данные для сохранения импортированной выписки.
 */
public record StatementPersistenceCommand(
        BankType bankType,
        String originalFileName,
        String maskedAccountNumber,
        String sourceFileHash,
        LocalDate periodFrom,
        LocalDate periodTo
) {

    public StatementPersistenceCommand {
        Objects.requireNonNull(bankType, "Bank type must not be null");
        Objects.requireNonNull(originalFileName, "Original file name must not be null");
        Objects.requireNonNull(sourceFileHash, "Source file hash must not be null");
        Objects.requireNonNull(periodFrom, "Period start must not be null");
        Objects.requireNonNull(periodTo, "Period end must not be null");
    }
}
