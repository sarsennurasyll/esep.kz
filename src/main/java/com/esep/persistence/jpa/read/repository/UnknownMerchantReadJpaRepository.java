package com.esep.persistence.jpa.read.repository;

import com.esep.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Read-репозиторий нераспознанных описаний операций.
 */
public interface UnknownMerchantReadJpaRepository extends Repository<Transaction, Long> {

    @Query("""
            select transaction.description as description, count(transaction) as usageCount
            from Transaction transaction
            where transaction.merchant is null
            group by transaction.description
            order by count(transaction) desc, transaction.description asc
            """)
    List<UnknownMerchantDescriptionProjection> findUnknownDescriptions();
}
