package com.esep.persistence.jpa.mapper;

import com.esep.entity.Statement;
import com.esep.persistence.model.StatementPersistenceCommand;
import org.springframework.stereotype.Component;

/**
 * Преобразует command-модель выписки в JPA-сущность.
 */
@Component
public class StatementJpaMapper {

    public Statement toEntity(StatementPersistenceCommand command) {
        return Statement.builder()
                .bankName(command.bankType())
                .originalFileName(command.originalFileName())
                .accountNumber(command.maskedAccountNumber())
                .sourceFileHash(command.sourceFileHash())
                .periodFrom(command.periodFrom())
                .periodTo(command.periodTo())
                .build();
    }
}
