package com.esep.persistence.jpa.read.mapper;

import com.esep.persistence.jpa.read.repository.StatementSummaryProjection;
import com.esep.statementimport.query.model.StatementQueryResult;
import org.springframework.stereotype.Component;

/**
 * Преобразует JPA-проекцию выписки в read-модель.
 */
@Component
public class StatementReadJpaMapper {

    public StatementQueryResult toQueryResult(StatementSummaryProjection projection) {
        return new StatementQueryResult(
                projection.getId(),
                projection.getBank(),
                projection.getOriginalFileName(),
                projection.getPeriodFrom(),
                projection.getPeriodTo(),
                projection.getTransactionCount(),
                projection.getImportedAt()
        );
    }
}
