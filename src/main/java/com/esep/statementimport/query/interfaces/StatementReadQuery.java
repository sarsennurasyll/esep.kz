package com.esep.statementimport.query.interfaces;

import com.esep.statementimport.query.model.StatementQueryResult;

import java.util.List;
import java.util.Optional;

/**
 * Прикладной контракт чтения импортированных выписок.
 */
public interface StatementReadQuery {

    List<StatementQueryResult> findAll();

    Optional<StatementQueryResult> findById(Long statementId);
}
