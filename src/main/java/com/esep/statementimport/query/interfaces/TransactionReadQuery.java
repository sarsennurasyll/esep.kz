package com.esep.statementimport.query.interfaces;

import com.esep.statementimport.query.model.TransactionQueryResult;

import java.util.List;

/**
 * Прикладной контракт чтения операций выписки.
 */
public interface TransactionReadQuery {

    List<TransactionQueryResult> findByStatementId(Long statementId);
}
