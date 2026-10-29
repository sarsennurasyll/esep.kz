package com.esep.analytics.service;

import com.esep.analytics.model.StatementCoverage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

/**
 * Определяет, покрыт ли календарный месяц периодами импортированных выписок полностью.
 * Несколько выписок вместе могут покрыть месяц, даже если ни одна не покрывает его целиком.
 */
public final class MonthCoverage {

    private MonthCoverage() {
    }

    public static boolean isComplete(YearMonth month, List<StatementCoverage> coverages) {
        LocalDate cursor = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<StatementCoverage> sorted = coverages.stream()
                .sorted(Comparator.comparing(StatementCoverage::periodFrom))
                .toList();
        for (StatementCoverage coverage : sorted) {
            if (coverage.periodFrom().isAfter(cursor)) {
                return false;
            }
            if (!coverage.periodTo().isBefore(cursor)) {
                cursor = coverage.periodTo().plusDays(1);
            }
            if (cursor.isAfter(end)) {
                return true;
            }
        }
        return false;
    }
}
