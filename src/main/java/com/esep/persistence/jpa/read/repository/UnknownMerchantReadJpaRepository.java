package com.esep.persistence.jpa.read.repository;

import com.esep.entity.BankOperationType;
import com.esep.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Read-репозиторий нераспознанных описаний операций, контрагентом которых может быть продавец.
 */
public interface UnknownMerchantReadJpaRepository extends Repository<Transaction, Long> {

    @Query("""
            select transaction.description as description,
                   count(transaction) as usageCount,
                   coalesce(sum(abs(transaction.amount)), 0) as totalAmount,
                   max(transaction.transactionDate) as lastTransactionDate,
                   case when exists (
                       select 1 from Transaction latest
                       where latest.merchant is null and latest.description = transaction.description
                         and latest.bankOperationType in :operationTypes
                         and latest.statement.uploadedAt = (select max(statement.uploadedAt) from Statement statement)
                   ) and not exists (
                       select 1 from Transaction earlier
                       where earlier.merchant is null and earlier.description = transaction.description
                         and earlier.bankOperationType in :operationTypes
                         and earlier.statement.uploadedAt < (select max(statement.uploadedAt) from Statement statement)
                   ) then true else false end as newInLatestStatement
            from Transaction transaction
            where transaction.merchant is null
              and transaction.bankOperationType in :operationTypes
            group by transaction.description
            order by count(transaction) desc, sum(abs(transaction.amount)) desc, max(transaction.transactionDate) desc
            """)
    List<UnknownMerchantDescriptionProjection> findUnknownDescriptions(
            @Param("operationTypes") Collection<BankOperationType> operationTypes
    );
}
