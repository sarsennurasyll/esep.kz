package com.esep.persistence.jpa.read.repository;

import com.esep.entity.Transaction;
import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.CounterpartyOperation;
import com.esep.moneyflow.model.LabelableOperation;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Запросы разметки назначения: агрегаты по контрагентам и массовое применение правил.
 */
public interface PurposeLabelingJpaRepository extends Repository<Transaction, Long> {

    @Query("""
            select new com.esep.moneyflow.model.LabelableOperation(
                       transaction.id, transaction.description, transaction.amount, transaction.flowType)
            from Transaction transaction
            where transaction.id = :operationId
            """)
    Optional<LabelableOperation> findOperation(@Param("operationId") long operationId);

    @Query("""
            select new com.esep.moneyflow.model.CounterpartyOperation(
                       transaction.id, transaction.statement.id, transaction.transactionDate, transaction.description,
                       transaction.amount, transaction.purpose, transaction.purposeSource)
            from Transaction transaction
            where transaction.description in :descriptions
              and transaction.flowType in :flowTypes
              and ((:inflow = true and transaction.amount > 0) or (:inflow = false and transaction.amount < 0))
            order by transaction.transactionDate desc, transaction.id desc
            """)
    List<CounterpartyOperation> findOperations(
            @Param("descriptions") Collection<String> descriptions,
            @Param("flowTypes") Collection<MoneyFlowType> flowTypes,
            @Param("inflow") boolean inflow
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Transaction transaction
            set transaction.purpose = :purpose, transaction.purposeSource = :source
            where transaction.id = :operationId
            """)
    int updateOperationPurpose(
            @Param("operationId") long operationId,
            @Param("purpose") MoneyPurpose purpose,
            @Param("source") ClassificationSource source
    );

    @Query("""
            select transaction.description as description, transaction.purpose as purpose, transaction.purposeSource as purposeSource,
                   coalesce(sum(transaction.amount), 0) as amount, count(transaction) as operationCount
            from Transaction transaction
            where transaction.amount > 0 and transaction.flowType in :flowTypes
            group by transaction.description, transaction.purpose, transaction.purposeSource
            """)
    List<CounterpartyActivityProjection> findInflowActivity(@Param("flowTypes") Collection<MoneyFlowType> flowTypes);

    @Query("""
            select transaction.description as description, transaction.purpose as purpose, transaction.purposeSource as purposeSource,
                   coalesce(sum(-transaction.amount), 0) as amount, count(transaction) as operationCount
            from Transaction transaction
            where transaction.amount < 0 and transaction.flowType in :flowTypes
            group by transaction.description, transaction.purpose, transaction.purposeSource
            """)
    List<CounterpartyActivityProjection> findOutflowActivity(@Param("flowTypes") Collection<MoneyFlowType> flowTypes);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Transaction transaction
            set transaction.purpose = :purpose, transaction.purposeSource = :source
            where transaction.description in :descriptions
              and transaction.flowType in :flowTypes
              and transaction.purposeSource <> com.esep.moneyflow.model.ClassificationSource.USER
              and ((:inflow = true and transaction.amount > 0) or (:inflow = false and transaction.amount < 0))
            """)
    int updatePurpose(
            @Param("descriptions") Collection<String> descriptions,
            @Param("flowTypes") Collection<MoneyFlowType> flowTypes,
            @Param("inflow") boolean inflow,
            @Param("purpose") MoneyPurpose purpose,
            @Param("source") ClassificationSource source
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Transaction transaction
            set transaction.purpose = com.esep.moneyflow.model.MoneyPurpose.UNKNOWN,
                transaction.purposeSource = com.esep.moneyflow.model.ClassificationSource.NONE
            where transaction.description in :descriptions
              and transaction.flowType in :flowTypes
              and transaction.purposeSource = com.esep.moneyflow.model.ClassificationSource.PERSONAL_RULE
              and ((:inflow = true and transaction.amount > 0) or (:inflow = false and transaction.amount < 0))
            """)
    int resetRulePurpose(
            @Param("descriptions") Collection<String> descriptions,
            @Param("flowTypes") Collection<MoneyFlowType> flowTypes,
            @Param("inflow") boolean inflow
    );
}
