package com.esep.persistence.jpa.adapter;

import com.esep.entity.Merchant;
import com.esep.entity.Statement;
import com.esep.persistence.interfaces.TransactionCatalog;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import com.esep.persistence.jpa.mapper.TransactionJpaMapper;
import com.esep.persistence.jpa.repository.MerchantJpaRepository;
import com.esep.persistence.jpa.repository.StatementJpaRepository;
import com.esep.persistence.jpa.repository.TransactionJpaRepository;
import com.esep.persistence.model.TransactionPersistenceCommand;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-адаптер прикладного каталога операций.
 */
@Repository
public class TransactionCatalogJpaAdapter implements TransactionCatalog {

    private final TransactionJpaRepository transactionJpaRepository;
    private final StatementJpaRepository statementJpaRepository;
    private final MerchantJpaRepository merchantJpaRepository;
    private final TransactionJpaMapper transactionJpaMapper;
    private final MerchantReferenceJpaMapper merchantReferenceJpaMapper;

    public TransactionCatalogJpaAdapter(
            TransactionJpaRepository transactionJpaRepository,
            StatementJpaRepository statementJpaRepository,
            MerchantJpaRepository merchantJpaRepository,
            TransactionJpaMapper transactionJpaMapper,
            MerchantReferenceJpaMapper merchantReferenceJpaMapper
    ) {
        this.transactionJpaRepository = transactionJpaRepository;
        this.statementJpaRepository = statementJpaRepository;
        this.merchantJpaRepository = merchantJpaRepository;
        this.transactionJpaMapper = transactionJpaMapper;
        this.merchantReferenceJpaMapper = merchantReferenceJpaMapper;
    }

    @Override
    public void saveAll(String statementSourceFileHash, List<TransactionPersistenceCommand> transactions) {
        Statement statement = statementJpaRepository.findBySourceFileHash(statementSourceFileHash)
                .orElseThrow(() -> new IllegalArgumentException("Statement was not found for the source file hash"));

        List<com.esep.entity.Transaction> entities = transactions.stream()
                .map(command -> transactionJpaMapper.toEntity(command, statement, resolveMerchant(command)))
                .toList();

        transactionJpaRepository.saveAll(entities);
    }

    private Merchant resolveMerchant(TransactionPersistenceCommand command) {
        if (command.merchantReference() == null) {
            return null;
        }

        Long merchantId = merchantReferenceJpaMapper.toMerchantId(command.merchantReference());
        return merchantJpaRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant was not found for the reference"));
    }
}
