package com.esep.persistence.interfaces;

import com.esep.persistence.model.StoredStatement;

import java.util.Collection;
import java.util.List;

/** Сохранённые выписки для удаления вместе с операциями. */
public interface StatementDeletionCatalog {

    /** Все выписки в порядке импорта. */
    List<StoredStatement> findAllStored();

    long countTransactions(Collection<Long> statementIds);

    /** Удаляет выписки вместе с их операциями; возвращает число удалённых операций. */
    long deleteWithTransactions(Collection<Long> statementIds);
}
