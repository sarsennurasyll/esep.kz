package com.esep.persistence.interfaces;

import com.esep.persistence.model.TransactionPersistenceCommand;

import com.esep.entity.BankType;
import com.esep.persistence.model.ExistingOperationKey;

import java.time.LocalDate;
import java.util.List;

/**
 * Прикладной контракт пакетного хранения операций выписки.
 */
public interface TransactionCatalog {

    void saveAll(String statementSourceFileHash, List<TransactionPersistenceCommand> transactions);

    /**
     * Возвращает признаки операций, уже сохранённых из выписок того же банка и счёта за указанный период.
     */
    List<ExistingOperationKey> findOperationKeys(BankType bankType, String maskedAccountNumber, LocalDate periodFrom, LocalDate periodTo);
}
