package com.esep.persistence.jpa.adapter;

import com.esep.persistence.interfaces.StatementCatalog;
import com.esep.persistence.jpa.mapper.StatementJpaMapper;
import com.esep.persistence.jpa.repository.StatementJpaRepository;
import com.esep.persistence.model.StatementPersistenceCommand;
import org.springframework.stereotype.Repository;

/**
 * JPA-адаптер прикладного каталога выписок.
 */
@Repository
public class StatementCatalogJpaAdapter implements StatementCatalog {

    private final StatementJpaRepository statementJpaRepository;
    private final StatementJpaMapper statementJpaMapper;

    public StatementCatalogJpaAdapter(
            StatementJpaRepository statementJpaRepository,
            StatementJpaMapper statementJpaMapper
    ) {
        this.statementJpaRepository = statementJpaRepository;
        this.statementJpaMapper = statementJpaMapper;
    }

    @Override
    public boolean existsBySourceFileHash(String sourceFileHash) {
        return statementJpaRepository.existsBySourceFileHash(sourceFileHash);
    }

    @Override
    public void save(StatementPersistenceCommand statement) {
        statementJpaRepository.save(statementJpaMapper.toEntity(statement));
    }
}
