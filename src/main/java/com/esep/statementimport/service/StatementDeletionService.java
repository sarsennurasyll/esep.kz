package com.esep.statementimport.service;

import com.esep.persistence.interfaces.StatementDeletionCatalog;
import com.esep.persistence.model.StoredStatement;
import com.esep.statementimport.exception.StatementDeletionMismatchException;
import com.esep.statementimport.exception.StatementNotFoundException;
import com.esep.statementimport.model.StatementDeletionPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Удаление выписки пользователем вместе с её операциями и решениями по ним.
 * Личные правила контрагентов и база продавцов не меняются: они не принадлежат одной выписке.
 */
@Service
public class StatementDeletionService {

    private final StatementDeletionCatalog catalog;

    public StatementDeletionService(StatementDeletionCatalog catalog) {
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public StatementDeletionPlan plan(long statementId) {
        List<StoredStatement> statements = catalog.findAllStored();
        StoredStatement target = statements.stream()
                .filter(statement -> statement.id() == statementId)
                .findFirst()
                .orElseThrow(() -> new StatementNotFoundException(statementId));
        List<Long> group = StatementDeletionGroups.of(target, statements);
        return new StatementDeletionPlan(statementId, group, catalog.countTransactions(group));
    }

    /**
     * Удаляет выписку и зависящие от неё выписки, если пользователь подтвердил именно этот набор.
     * Набор мог измениться после показа подтверждения — тогда ничего не удаляется.
     */
    @Transactional
    public StatementDeletionPlan delete(long statementId, Collection<Long> confirmedStatementIds) {
        StatementDeletionPlan plan = plan(statementId);
        if (!new HashSet<>(plan.statementIds()).equals(new HashSet<>(confirmedStatementIds))) {
            throw new StatementDeletionMismatchException(plan);
        }
        long deleted = catalog.deleteWithTransactions(plan.statementIds());
        return new StatementDeletionPlan(statementId, plan.statementIds(), deleted);
    }
}
