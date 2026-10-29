package com.esep.persistence.jpa.repository;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.persistence.model.ExistingOperationKey;
import com.esep.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<Transaction, Long> {

    long countByMerchantId(Long merchantId);

    long countByStatementIdIn(Collection<Long> statementIds);

    @Query("""
            select transaction.merchant.id as merchantId,
                   count(transaction) as transactionCount,
                   coalesce(sum(abs(transaction.amount)), 0) as totalAmount,
                   min(transaction.transactionDate) as firstTransactionDate,
                   max(transaction.transactionDate) as lastTransactionDate
            from Transaction transaction
            where transaction.merchant.id in :merchantIds
            group by transaction.merchant.id
            """)
    List<MerchantActivityProjection> findMerchantActivity(@Param("merchantIds") Collection<Long> merchantIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Transaction transaction where transaction.statement.id in :statementIds")
    int deleteByStatementIds(@Param("statementIds") Collection<Long> statementIds);

    List<Transaction> findByMerchantIsNullAndDescriptionInAndBankOperationTypeIn(
            Collection<String> descriptions,
            Collection<BankOperationType> bankOperationTypes
    );

    @Query("""
            select new com.esep.persistence.model.ExistingOperationKey(
                       transaction.transactionDate, transaction.amount, transaction.bankOperationType, transaction.description)
            from Transaction transaction
            where transaction.statement.bankName = :bankType
              and transaction.statement.accountNumber = :maskedAccountNumber
              and transaction.transactionDate between :periodFrom and :periodTo
            """)
    List<ExistingOperationKey> findOperationKeys(
            @Param("bankType") BankType bankType,
            @Param("maskedAccountNumber") String maskedAccountNumber,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo
    );
}
