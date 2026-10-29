package com.esep.statementimport.service;

import com.esep.entity.BankOperationType;
import com.esep.persistence.model.ExistingOperationKey;
import com.esep.statementimport.model.ParsedTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OverlappingOperationFilterTest {

    private static final LocalDate DAY = LocalDate.of(2026, 8, 10);

    private final OverlappingOperationFilter filter = new OverlappingOperationFilter();

    @Test
    void shouldSkipOperationsAlreadyStoredFromPreviousStatement() {
        var result = filter.filter(
                List.of(parsed("-1000", "TEST STORE", 0), parsed("-500", "OTHER STORE", 1)),
                List.of(existing("-1000.00", "TEST STORE"))
        );

        assertThat(result.skippedDuplicates()).isEqualTo(1);
        assertThat(result.newTransactions()).extracting(ParsedTransaction::description).containsExactly("OTHER STORE");
    }

    @Test
    void shouldKeepLegitimateRepeatsBeyondStoredCount() {
        // Три одинаковые поездки за день в новой выписке, в прежней было сохранено две.
        var result = filter.filter(
                List.of(parsed("-120", "TEST TRANSIT", 0), parsed("-120", "TEST TRANSIT", 1), parsed("-120", "TEST TRANSIT", 2)),
                List.of(existing("-120", "TEST TRANSIT"), existing("-120", "TEST TRANSIT"))
        );

        assertThat(result.skippedDuplicates()).isEqualTo(2);
        assertThat(result.newTransactions()).hasSize(1);
    }

    @Test
    void shouldNotTreatDifferentTypeOrAmountAsDuplicate() {
        var result = filter.filter(
                List.of(
                        new ParsedTransaction(DAY, "Тестов Т.", new BigDecimal("1000"), "KZT", BankOperationType.TOP_UP, 0),
                        parsed("-1001", "TEST STORE", 1)
                ),
                List.of(
                        new ExistingOperationKey(DAY, new BigDecimal("-1000"), BankOperationType.TRANSFER, "Тестов Т."),
                        existing("-1000", "TEST STORE")
                )
        );

        assertThat(result.skippedDuplicates()).isZero();
        assertThat(result.newTransactions()).hasSize(2);
    }

    @Test
    void shouldIgnoreWhitespaceDifferencesInDescription() {
        var result = filter.filter(List.of(parsed("-1000", "TEST  STORE ", 0)), List.of(existing("-1000", "TEST STORE")));

        assertThat(result.skippedDuplicates()).isEqualTo(1);
    }

    @Test
    void shouldCountOperationsMissingFromNewStatementExceptBoundaryDays() {
        LocalDate first = DAY.minusDays(1);
        LocalDate last = DAY.plusDays(1);
        var result = filter.filter(
                List.of(parsed("-1000", "TEST STORE", 0)),
                List.of(
                        existing("-1000", "TEST STORE"),
                        existing("-1000", "TEST STORE"),
                        existing("-500", "OTHER STORE"),
                        new ExistingOperationKey(first, new BigDecimal("-700"), BankOperationType.PURCHASE, "EARLY STORE"),
                        new ExistingOperationKey(last, new BigDecimal("-900"), BankOperationType.PURCHASE, "LATE STORE")
                ),
                first,
                last
        );

        assertThat(result.skippedDuplicates()).isEqualTo(1);
        assertThat(result.missingFromNewStatement()).isEqualTo(2);
    }

    private ParsedTransaction parsed(String amount, String description, int position) {
        return new ParsedTransaction(DAY, description, new BigDecimal(amount), "KZT", BankOperationType.PURCHASE, position);
    }

    private ExistingOperationKey existing(String amount, String description) {
        return new ExistingOperationKey(DAY, new BigDecimal(amount), BankOperationType.PURCHASE, description);
    }
}
