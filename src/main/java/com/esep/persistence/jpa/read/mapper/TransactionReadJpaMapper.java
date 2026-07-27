package com.esep.persistence.jpa.read.mapper;

import com.esep.entity.Transaction;
import com.esep.statementimport.query.model.TransactionQueryResult;
import org.springframework.stereotype.Component;

/**
 * Преобразует JPA-сущность операции в read-модель.
 */
@Component
public class TransactionReadJpaMapper {

    private static final String UNCATEGORIZED = "UNCATEGORIZED";

    public TransactionQueryResult toQueryResult(Transaction transaction) {
        String merchant = transaction.getMerchant() == null ? null : transaction.getMerchant().getOriginalName();
        String category = transaction.getMerchant() == null || transaction.getMerchant().getCategory() == null
                ? UNCATEGORIZED
                : transaction.getMerchant().getCategory().getCode();

        return new TransactionQueryResult(
                transaction.getTransactionDate(),
                transaction.getDescription(),
                merchant,
                category,
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionType()
        );
    }
}
