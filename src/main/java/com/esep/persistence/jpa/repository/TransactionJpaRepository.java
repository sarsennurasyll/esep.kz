package com.esep.persistence.jpa.repository;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.persistence.model.ExistingOperationKey;
import com.esep.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransactionJpaRepository extends JpaRepository<Transaction, Long> {

    long countByMerchantId(Long merchantId);

    long countByStatementIdIn(Collection<Long> statementIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Transaction transaction where transaction.statement.id in :statementIds")
    int deleteByStatementIds(@Param("statementIds") Collection<Long> statementIds);

    List<Transaction> findByMerchantIsNullAndDescriptionInAndBankOperationTypeIn(
            Collection<String> descriptions,
            Collection<BankOperationType> bankOperationTypes
    );

    @Query("select coalesce(sum(abs(transaction.amount)), 0) from Transaction transaction where transaction.merchant.id = :merchantId")
    BigDecimal sumAbsoluteAmountByMerchantId(Long merchantId);

    @Query("select min(transaction.transactionDate) from Transaction transaction where transaction.merchant.id = :merchantId")
    Optional<LocalDate> findFirstTransactionDateByMerchantId(Long merchantId);

    @Query("select max(transaction.transactionDate) from Transaction transaction where transaction.merchant.id = :merchantId")
    Optional<LocalDate> findLastTransactionDateByMerchantId(Long merchantId);

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
