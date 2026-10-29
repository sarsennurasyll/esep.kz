package com.esep.persistence.jpa.repository;

import com.esep.entity.Statement;
import com.esep.persistence.model.StoredStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StatementJpaRepository extends JpaRepository<Statement, Long> {

    boolean existsBySourceFileHash(String sourceFileHash);

    Optional<Statement> findBySourceFileHash(String sourceFileHash);

    @Query("""
            select new com.esep.persistence.model.StoredStatement(
                       statement.id, statement.bankName, statement.accountNumber, statement.periodFrom, statement.periodTo)
            from Statement statement
            order by statement.id
            """)
    List<StoredStatement> findAllStored();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Statement statement where statement.id in :statementIds")
    int deleteByIds(@Param("statementIds") Collection<Long> statementIds);
}
