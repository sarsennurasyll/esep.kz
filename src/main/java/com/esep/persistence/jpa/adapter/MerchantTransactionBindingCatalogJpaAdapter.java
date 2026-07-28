package com.esep.persistence.jpa.adapter;

import com.esep.entity.Merchant;
import com.esep.entity.Transaction;
import com.esep.merchantmanagement.interfaces.MerchantTransactionBindingCatalog;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import com.esep.persistence.jpa.repository.MerchantJpaRepository;
import com.esep.persistence.jpa.repository.TransactionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * JPA-адаптер массового связывания исторических операций с продавцом.
 */
@Repository
public class MerchantTransactionBindingCatalogJpaAdapter implements MerchantTransactionBindingCatalog {

    private final TransactionJpaRepository transactionJpaRepository;
    private final MerchantJpaRepository merchantJpaRepository;
    private final MerchantReferenceJpaMapper merchantReferenceJpaMapper;

    public MerchantTransactionBindingCatalogJpaAdapter(
            TransactionJpaRepository transactionJpaRepository,
            MerchantJpaRepository merchantJpaRepository,
            MerchantReferenceJpaMapper merchantReferenceJpaMapper
    ) {
        this.transactionJpaRepository = transactionJpaRepository;
        this.merchantJpaRepository = merchantJpaRepository;
        this.merchantReferenceJpaMapper = merchantReferenceJpaMapper;
    }

    @Override
    public long bindUnknownTransactions(Collection<String> descriptions, MerchantReference merchantReference) {
        if (descriptions.isEmpty()) {
            return 0;
        }

        Merchant merchant = merchantJpaRepository.findById(merchantReferenceJpaMapper.toMerchantId(merchantReference))
                .orElseThrow(() -> new IllegalArgumentException("Merchant was not found for the reference"));
        List<Transaction> transactions = transactionJpaRepository.findByMerchantIsNullAndDescriptionIn(descriptions);
        transactions.forEach(transaction -> transaction.setMerchant(merchant));
        return transactions.size();
    }
}
