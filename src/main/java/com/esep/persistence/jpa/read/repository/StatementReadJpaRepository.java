package com.esep.persistence.jpa.read.repository;

import com.esep.entity.Statement;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StatementReadJpaRepository extends Repository<Statement, Long> {

    @Query("""
            select statement.id as id,
                   statement.bankName as bank,
                   statement.originalFileName as originalFileName,
                   statement.periodFrom as periodFrom,
                   statement.periodTo as periodTo,
                   count(transaction.id) as transactionCount,
                   statement.uploadedAt as importedAt
            from Statement statement
            left join Transaction transaction on transaction.statement = statement
            group by statement.id, statement.bankName, statement.originalFileName,
                     statement.periodFrom, statement.periodTo, statement.uploadedAt
            order by statement.uploadedAt desc
            """)
    List<StatementSummaryProjection> findAllSummaries();

    @Query("""
            select statement.id as id,
                   statement.bankName as bank,
                   statement.originalFileName as originalFileName,
                   statement.periodFrom as periodFrom,
                   statement.periodTo as periodTo,
                   count(transaction.id) as transactionCount,
                   statement.uploadedAt as importedAt
            from Statement statement
            left join Transaction transaction on transaction.statement = statement
            where statement.id = :statementId
            group by statement.id, statement.bankName, statement.originalFileName,
                     statement.periodFrom, statement.periodTo, statement.uploadedAt
            """)
    Optional<StatementSummaryProjection> findSummaryById(@Param("statementId") Long statementId);
}
