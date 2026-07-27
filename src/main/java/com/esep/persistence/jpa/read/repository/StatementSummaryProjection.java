package com.esep.persistence.jpa.read.repository;

import com.esep.entity.BankType;

import java.time.Instant;
import java.time.LocalDate;

public interface StatementSummaryProjection {

    Long getId();

    BankType getBank();

    String getOriginalFileName();

    LocalDate getPeriodFrom();

    LocalDate getPeriodTo();

    long getTransactionCount();

    Instant getImportedAt();
}
