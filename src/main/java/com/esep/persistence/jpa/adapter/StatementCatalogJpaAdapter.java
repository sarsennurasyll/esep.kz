package com.esep.persistence.jpa.adapter;

import com.esep.persistence.interfaces.StatementCatalog;
import com.esep.persistence.jpa.mapper.StatementJpaMapper;
import com.esep.persistence.jpa.repository.StatementJpaRepository;
import com.esep.persistence.model.StatementPersistenceCommand;
import com.esep.statementimport.exception.StatementAlreadyImportedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Locale;

/**
 * JPA-адаптер прикладного каталога выписок.
 */
@Repository
public class StatementCatalogJpaAdapter implements StatementCatalog {

    private static final String SOURCE_FILE_HASH_COLUMN = "source_file_hash";

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
    /**
     * Проверка {@code existsBySourceFileHash} и вставка не атомарны: при одновременной загрузке одного файла
     * второй запрос упирается в уникальный индекс. Это тот же случай «уже импортирована», а не ошибка сервера.
     */
    public Long save(StatementPersistenceCommand statement) {
        try {
            return statementJpaRepository.save(statementJpaMapper.toEntity(statement)).getId();
        } catch (DataIntegrityViolationException exception) {
            if (violatesSourceFileHash(exception)) {
                throw new StatementAlreadyImportedException(statement.sourceFileHash());
            }
            throw exception;
        }
    }

    private static boolean violatesSourceFileHash(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause().getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains(SOURCE_FILE_HASH_COLUMN);
    }
}
