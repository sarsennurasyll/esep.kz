package com.esep.persistence.interfaces;

import com.esep.persistence.model.StatementPersistenceCommand;

/**
 * Прикладной контракт хранения импортированных выписок.
 */
public interface StatementCatalog {

    boolean existsBySourceFileHash(String sourceFileHash);

    Long save(StatementPersistenceCommand statement);
}
