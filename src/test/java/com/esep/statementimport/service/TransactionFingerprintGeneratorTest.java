package com.esep.statementimport.service;

import com.esep.statementimport.model.ParsedTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionFingerprintGeneratorTest {

    private final TransactionFingerprintGenerator fingerprintGenerator = new TransactionFingerprintGenerator();

    @Test
    void shouldGenerateExpectedDeterministicFingerprint() {
        ParsedTransaction transaction = new ParsedTransaction(
                LocalDate.of(2026, 7, 12),
                "Magnum",
                new BigDecimal("14500.00"),
                "KZT"
        );

        String fingerprint = fingerprintGenerator.generate("a".repeat(64), transaction, "MAGNUM");

        assertThat(fingerprint).isEqualTo("77cc21f69438946149bef539f7bafd42c6c7960147e664644f7dcc056ad7d31d");
    }

    @Test
    void shouldChangeFingerprintWhenNormalizedDescriptionChanges() {
        ParsedTransaction transaction = new ParsedTransaction(
                LocalDate.of(2026, 7, 12),
                "Magnum",
                new BigDecimal("14500.00"),
                "KZT"
        );

        String first = fingerprintGenerator.generate("a".repeat(64), transaction, "MAGNUM");
        String second = fingerprintGenerator.generate("a".repeat(64), transaction, "MAGNUM EXPRESS");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldChangeFingerprintWhenSourceRecordPositionChanges() {
        ParsedTransaction firstTransaction = new ParsedTransaction(
                LocalDate.of(2026, 7, 12),
                "MAGNUM",
                new BigDecimal("14500.00"),
                "KZT",
                0
        );
        ParsedTransaction secondTransaction = new ParsedTransaction(
                LocalDate.of(2026, 7, 12),
                "MAGNUM",
                new BigDecimal("14500.00"),
                "KZT",
                1
        );

        String first = fingerprintGenerator.generate("a".repeat(64), firstTransaction, "MAGNUM");
        String second = fingerprintGenerator.generate("a".repeat(64), secondTransaction, "MAGNUM");

        assertThat(first).isNotEqualTo(second);
    }
}
