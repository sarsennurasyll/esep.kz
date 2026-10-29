package com.esep.statementimport.service;

import java.time.LocalDate;

/**
 * Вычисленный период операций банковской выписки.
 */
public record StatementPeriod(LocalDate periodFrom, LocalDate periodTo) {
}
