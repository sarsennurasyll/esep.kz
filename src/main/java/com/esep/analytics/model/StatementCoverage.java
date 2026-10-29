package com.esep.analytics.model;

import java.time.LocalDate;
import java.util.Objects;

/** Период, за который есть операции в импортированной выписке. */
public record StatementCoverage(LocalDate periodFrom, LocalDate periodTo) {

    public StatementCoverage {
        Objects.requireNonNull(periodFrom, "Period start must not be null");
        Objects.requireNonNull(periodTo, "Period end must not be null");
    }
}
