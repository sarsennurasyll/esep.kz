package com.esep.persistence.jpa.read.repository;

import com.esep.entity.Transaction;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionReadJpaRepository extends Repository<Transaction, Long> {

    @Query("""
            select transaction
            from Transaction transaction
            left join fetch transaction.merchant merchant
            left join fetch merchant.category
            where transaction.statement.id = :statementId
            order by transaction.transactionDate asc, transaction.id asc
            """)
    List<Transaction> findByStatementIdWithMerchantAndCategory(@Param("statementId") Long statementId);
}
