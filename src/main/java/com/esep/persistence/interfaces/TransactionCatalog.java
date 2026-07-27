package com.esep.persistence.interfaces;

import com.esep.persistence.model.TransactionPersistenceCommand;

import java.util.List;

/**
 * Прикладной контракт пакетного хранения операций выписки.
 */
public interface TransactionCatalog {

    void saveAll(String statementSourceFileHash, List<TransactionPersistenceCommand> transactions);
}
