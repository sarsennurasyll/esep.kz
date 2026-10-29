package com.esep.persistence.jpa.read.repository;

import com.esep.analytics.model.StatementCoverage;
import com.esep.entity.Statement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Read-репозиторий агрегатов выписок.
 */
public interface AnalyticsStatementReadJpaRepository extends Repository<Statement, Long> {

    @Query("select count(statement) from Statement statement")
    long countStatements();

    @Query("select new com.esep.analytics.model.StatementCoverage(statement.periodFrom, statement.periodTo) from Statement statement")
    List<StatementCoverage> findStatementCoverages();
}
