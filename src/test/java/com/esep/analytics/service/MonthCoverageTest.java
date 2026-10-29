package com.esep.analytics.service;

import com.esep.analytics.model.StatementCoverage;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthCoverageTest {

    private static final YearMonth AUGUST = YearMonth.of(2026, 8);

    @Test
    void shouldTreatMonthsAtEdgesOfStatementPeriodAsIncomplete() {
        List<StatementCoverage> kaspiHalfYear = List.of(coverage("2026-04-25", "2026-10-25"));

        assertThat(MonthCoverage.isComplete(YearMonth.of(2026, 4), kaspiHalfYear)).isFalse();
        assertThat(MonthCoverage.isComplete(AUGUST, kaspiHalfYear)).isTrue();
        assertThat(MonthCoverage.isComplete(YearMonth.of(2026, 10), kaspiHalfYear)).isFalse();
    }

    @Test
    void shouldCombineAdjacentAndOverlappingStatements() {
        assertThat(MonthCoverage.isComplete(AUGUST, List.of(
                coverage("2026-08-16", "2026-09-15"),
                coverage("2026-07-16", "2026-08-15")
        ))).isTrue();
        assertThat(MonthCoverage.isComplete(AUGUST, List.of(
                coverage("2026-07-01", "2026-08-20"),
                coverage("2026-08-10", "2026-08-31")
        ))).isTrue();
    }

    @Test
    void shouldDetectGapOfOneDay() {
        assertThat(MonthCoverage.isComplete(AUGUST, List.of(
                coverage("2026-08-01", "2026-08-15"),
                coverage("2026-08-17", "2026-08-31")
        ))).isFalse();
    }

    @Test
    void shouldTreatMonthWithoutStatementsAsIncomplete() {
        assertThat(MonthCoverage.isComplete(AUGUST, List.of())).isFalse();
    }

    private StatementCoverage coverage(String from, String to) {
        return new StatementCoverage(LocalDate.parse(from), LocalDate.parse(to));
    }
}
