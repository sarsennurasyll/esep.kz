package com.esep.persistence.jpa.read.adapter;

import com.esep.persistence.jpa.read.mapper.TransactionReadJpaMapper;
import com.esep.persistence.jpa.read.repository.TransactionReadJpaRepository;
import com.esep.statementimport.query.interfaces.TransactionReadQuery;
import com.esep.statementimport.query.model.TransactionQueryResult;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-адаптер сценария чтения операций выписки.
 */
@Repository
public class TransactionReadJpaAdapter implements TransactionReadQuery {

    private final TransactionReadJpaRepository transactionReadJpaRepository;
    private final TransactionReadJpaMapper transactionReadJpaMapper;

    public TransactionReadJpaAdapter(
            TransactionReadJpaRepository transactionReadJpaRepository,
            TransactionReadJpaMapper transactionReadJpaMapper
    ) {
        this.transactionReadJpaRepository = transactionReadJpaRepository;
        this.transactionReadJpaMapper = transactionReadJpaMapper;
    }

    @Override
    public List<TransactionQueryResult> findByStatementId(Long statementId) {
        return transactionReadJpaRepository.findByStatementIdWithMerchantAndCategory(statementId).stream()
                .map(transactionReadJpaMapper::toQueryResult)
                .toList();
    }
}
