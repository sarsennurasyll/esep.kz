package com.esep.statementimport.service;

import java.time.LocalDate;

/**
 * Вычисленный период операций банковской выписки.
 */
record StatementPeriod(LocalDate periodFrom, LocalDate periodTo) {
}
