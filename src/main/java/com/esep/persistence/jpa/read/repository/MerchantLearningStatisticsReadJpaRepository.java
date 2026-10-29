package com.esep.persistence.jpa.read.repository;

import com.esep.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface MerchantLearningStatisticsReadJpaRepository extends Repository<Transaction, Long> {

    @Query("""
            select (select count(merchant) from Merchant merchant) as merchantCount,
                   (select count(alias) from MerchantAlias alias) as aliasCount,
                   coalesce(sum(case when transaction.merchant is not null then 1 else 0 end), 0) as purchasesWithMerchantCount,
                   coalesce(sum(case when transaction.merchant is null then 1 else 0 end), 0) as purchasesWithoutMerchantCount
            from Transaction transaction
            where transaction.flowType in (com.esep.moneyflow.model.MoneyFlowType.PURCHASE, com.esep.moneyflow.model.MoneyFlowType.PURCHASE_REFUND)
            """)
    MerchantLearningStatisticsProjection getStatistics();
}
