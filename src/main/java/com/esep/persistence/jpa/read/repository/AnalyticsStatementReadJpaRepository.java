package com.esep.persistence.jpa.read.repository;

import com.esep.entity.Statement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * Read-репозиторий агрегатов выписок.
 */
public interface AnalyticsStatementReadJpaRepository extends Repository<Statement, Long> {

    @Query("select count(statement) from Statement statement")
    long countStatements();
}
