package com.esep.persistence.jpa.adapter;

import com.esep.persistence.interfaces.StatementDeletionCatalog;
import com.esep.persistence.jpa.repository.StatementJpaRepository;
import com.esep.persistence.jpa.repository.TransactionJpaRepository;
import com.esep.persistence.model.StoredStatement;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * JPA-адаптер удаления выписок: сначала операции, затем сами выписки.
 */
@Repository
public class StatementDeletionCatalogJpaAdapter implements StatementDeletionCatalog {

    private final StatementJpaRepository statementJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;

    public StatementDeletionCatalogJpaAdapter(
            StatementJpaRepository statementJpaRepository,
            TransactionJpaRepository transactionJpaRepository
    ) {
        this.statementJpaRepository = statementJpaRepository;
        this.transactionJpaRepository = transactionJpaRepository;
    }

    @Override
    public List<StoredStatement> findAllStored() {
        return statementJpaRepository.findAllStored();
    }

    @Override
    public long countTransactions(Collection<Long> statementIds) {
        return statementIds.isEmpty() ? 0 : transactionJpaRepository.countByStatementIdIn(statementIds);
    }

    @Override
    public long deleteWithTransactions(Collection<Long> statementIds) {
        if (statementIds.isEmpty()) {
            return 0;
        }
        long deleted = transactionJpaRepository.deleteByStatementIds(statementIds);
        statementJpaRepository.deleteByIds(statementIds);
        return deleted;
    }
}
