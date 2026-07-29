package com.esep.persistence.model;

import com.esep.entity.TransactionType;
import com.esep.entity.BankOperationType;
import com.esep.merchantresolver.model.MerchantReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Входные данные для сохранения операции импортированной выписки.
 */
public record TransactionPersistenceCommand(
        LocalDate transactionDate,
        String description,
        BigDecimal amount,
        String currency,
        TransactionType transactionType,
        BankOperationType bankOperationType,
        MerchantReference merchantReference,
        String sourceTransactionFingerprint
) {

    public TransactionPersistenceCommand {
        Objects.requireNonNull(transactionDate, "Transaction date must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        Objects.requireNonNull(transactionType, "Transaction type must not be null");
        Objects.requireNonNull(bankOperationType, "Bank operation type must not be null");
        Objects.requireNonNull(sourceTransactionFingerprint, "Transaction fingerprint must not be null");
    }

    public TransactionPersistenceCommand(LocalDate transactionDate, String description, BigDecimal amount, String currency,
                                         TransactionType transactionType, MerchantReference merchantReference, String sourceTransactionFingerprint) {
        this(transactionDate, description, amount, currency, transactionType, BankOperationType.UNKNOWN, merchantReference, sourceTransactionFingerprint);
    }
}
