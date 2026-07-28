package com.esep.persistence.jpa.read.repository;

import com.esep.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface MerchantLearningStatisticsReadJpaRepository extends Repository<Transaction, Long> {

    @Query("""
            select (select count(merchant) from Merchant merchant) as merchantCount,
                   (select count(alias) from MerchantAlias alias) as aliasCount,
                   count(transaction) - coalesce(sum(case when transaction.merchant is null then 1 else 0 end), 0) as recognizedTransactionCount,
                   coalesce(sum(case when transaction.merchant is null then 1 else 0 end), 0) as unknownTransactionCount
            from Transaction transaction
            """)
    MerchantLearningStatisticsProjection getStatistics();
}
