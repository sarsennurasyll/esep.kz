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

        assertThat(fingerprint).isEqualTo("502fadbbecf238aa4539c19ea103c2f0cbed654127b85ed6e80d8d18e3368d02");
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
}
