package com.esep.statementimport.service;

import com.esep.statementimport.model.ParsedTransaction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Формирует детерминированный отпечаток операции для защиты от дубликатов.
 * Позиция исходной записи используется, когда выписка не содержит банковского идентификатора операции.
 */
public class TransactionFingerprintGenerator {

    public String generate(
            String sourceFileHash,
            ParsedTransaction transaction,
            String normalizedDescription
    ) {
        String fingerprintSource = String.join(
                "\n",
                sourceFileHash,
                String.valueOf(transaction.sourceRecordPosition()),
                transaction.date().toString(),
                transaction.amount().stripTrailingZeros().toPlainString(),
                transaction.currency(),
                normalizedDescription
        );

        return toSha256(fingerprintSource);
    }

    private String toSha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
