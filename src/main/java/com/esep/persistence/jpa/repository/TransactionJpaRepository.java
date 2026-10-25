package com.esep.persistence.jpa.repository;

import com.esep.entity.BankOperationType;
import com.esep.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransactionJpaRepository extends JpaRepository<Transaction, Long> {

    long countByMerchantId(Long merchantId);

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
}
