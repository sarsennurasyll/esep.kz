package com.esep.persistence.jpa.mapper;

import com.esep.entity.Merchant;
import com.esep.entity.Statement;
import com.esep.entity.Transaction;
import com.esep.persistence.model.TransactionPersistenceCommand;
import org.springframework.stereotype.Component;

/**
 * Преобразует command-модель операции в JPA-сущность.
 */
@Component
public class TransactionJpaMapper {

    public Transaction toEntity(
            TransactionPersistenceCommand command,
            Statement statement,
            Merchant merchant
    ) {
        return Transaction.builder()
                .transactionDate(command.transactionDate())
                .description(command.description())
                .amount(command.amount())
                .currency(command.currency())
                .transactionType(command.transactionType())
                .merchant(merchant)
                .sourceTransactionFingerprint(command.sourceTransactionFingerprint())
                .statement(statement)
                .build();
    }
}
