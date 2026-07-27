package com.esep.persistence.jpa.read.adapter;

import com.esep.persistence.jpa.read.mapper.StatementReadJpaMapper;
import com.esep.persistence.jpa.read.repository.StatementReadJpaRepository;
import com.esep.statementimport.query.interfaces.StatementReadQuery;
import com.esep.statementimport.query.model.StatementQueryResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-адаптер сценариев чтения выписок.
 */
@Repository
public class StatementReadJpaAdapter implements StatementReadQuery {

    private final StatementReadJpaRepository statementReadJpaRepository;
    private final StatementReadJpaMapper statementReadJpaMapper;

    public StatementReadJpaAdapter(
            StatementReadJpaRepository statementReadJpaRepository,
            StatementReadJpaMapper statementReadJpaMapper
    ) {
        this.statementReadJpaRepository = statementReadJpaRepository;
        this.statementReadJpaMapper = statementReadJpaMapper;
    }

    @Override
    public List<StatementQueryResult> findAll() {
        return statementReadJpaRepository.findAllSummaries().stream()
                .map(statementReadJpaMapper::toQueryResult)
                .toList();
    }

    @Override
    public Optional<StatementQueryResult> findById(Long statementId) {
        return statementReadJpaRepository.findSummaryById(statementId)
                .map(statementReadJpaMapper::toQueryResult);
    }
}
