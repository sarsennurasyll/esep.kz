package com.esep.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceModelTest {

    @Test
    void shouldKeepCategoryCodeAsStableModelAttribute() {
        Category category = Category.builder()
                .code("GROCERY")
                .name("Продукты")
                .build();

        assertThat(category.getCode()).isEqualTo("GROCERY");
    }

    @Test
    void shouldStoreOnlyMaskedAccountNumberAndFileHashInStatementModel() {
        Statement statement = Statement.builder()
                .bankName(BankType.KASPI)
                .originalFileName("statement.pdf")
                .accountNumber("****1234")
                .sourceFileHash("a".repeat(64))
                .periodFrom(LocalDate.of(2026, 7, 1))
                .periodTo(LocalDate.of(2026, 7, 31))
                .build();

        assertThat(statement.getAccountNumber()).isEqualTo("****1234");
        assertThat(statement.getSourceFileHash()).hasSize(64);
    }

    @Test
    void shouldRepresentTransactionDateWithoutArtificialTime() {
        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("14500.00"))
                .currency("KZT")
                .transactionDate(LocalDate.of(2026, 7, 12))
                .sourceTransactionFingerprint("b".repeat(64))
                .description("MAGNUM")
                .transactionType(TransactionType.EXPENSE)
                .statement(Statement.builder()
                        .bankName(BankType.KASPI)
                        .originalFileName("statement.pdf")
                        .sourceFileHash("a".repeat(64))
                        .periodFrom(LocalDate.of(2026, 7, 1))
                        .periodTo(LocalDate.of(2026, 7, 31))
                        .build())
                .build();

        assertThat(transaction.getTransactionDate()).isEqualTo(LocalDate.of(2026, 7, 12));
    }
}
