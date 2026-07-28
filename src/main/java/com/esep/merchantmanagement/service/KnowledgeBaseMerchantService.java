package com.esep.merchantmanagement.service;

import com.esep.entity.Category;
import com.esep.entity.Merchant;
import com.esep.entity.MerchantAlias;
import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.exception.MerchantDeletionNotAllowedException;
import com.esep.merchantmanagement.exception.MerchantNotFoundException;
import com.esep.merchantmanagement.model.KnowledgeBaseAlias;
import com.esep.merchantmanagement.model.KnowledgeBaseAliasCommand;
import com.esep.merchantmanagement.model.KnowledgeBaseMerchant;
import com.esep.merchantmanagement.model.KnowledgeBaseMerchantCommand;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import com.esep.persistence.jpa.repository.CategoryJpaRepository;
import com.esep.persistence.jpa.repository.MerchantAliasJpaRepository;
import com.esep.persistence.jpa.repository.MerchantJpaRepository;
import com.esep.persistence.jpa.repository.TransactionJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class KnowledgeBaseMerchantService {
    private final MerchantJpaRepository merchants;
    private final MerchantAliasJpaRepository aliases;
    private final CategoryJpaRepository categories;
    private final TransactionJpaRepository transactions;
    private final MerchantNormalizer normalizer;
    private final MerchantReferenceJpaMapper referenceMapper;

    public KnowledgeBaseMerchantService(MerchantJpaRepository merchants, MerchantAliasJpaRepository aliases,
                                        CategoryJpaRepository categories, TransactionJpaRepository transactions,
                                        MerchantNormalizer normalizer, MerchantReferenceJpaMapper referenceMapper) {
        this.merchants = merchants; this.aliases = aliases; this.categories = categories; this.transactions = transactions;
        this.normalizer = normalizer; this.referenceMapper = referenceMapper;
    }

    public List<KnowledgeBaseMerchant> findAll(String query, MerchantType merchantType, String categoryCode) {
        String term = query == null ? "" : query.trim().toUpperCase();
        return merchants.findAllForKnowledgeBase().stream()
                .filter(merchant -> merchantType == null || merchant.getMerchantType() == merchantType)
                .filter(merchant -> categoryCode == null || categoryCode.isBlank() || merchant.getCategory().getCode().equals(categoryCode))
                .filter(merchant -> term.isBlank() || matches(merchant, term))
                .map(this::toModel).toList();
    }

    public KnowledgeBaseMerchant findById(String reference) { return toModel(findMerchant(reference)); }

    @Transactional
    public MerchantReference create(KnowledgeBaseMerchantCommand command) {
        String normalized = normalized(command.name());
        if (merchants.findByNormalizedName(normalized).isPresent()) throw new IllegalArgumentException("Merchant already exists");
        Merchant merchant = Merchant.builder().originalName(command.name().trim()).normalizedName(normalized)
                .merchantType(command.merchantType()).category(category(command.categoryCode())).build();
        return referenceMapper.toReference(merchants.save(merchant).getId());
    }

    @Transactional
    public void update(String reference, KnowledgeBaseMerchantCommand command) {
        Merchant merchant = findMerchant(reference);
        String normalized = normalized(command.name());
        merchants.findByNormalizedName(normalized).filter(other -> !other.getId().equals(merchant.getId()))
                .ifPresent(other -> { throw new IllegalArgumentException("Merchant already exists"); });
        merchant.setOriginalName(command.name().trim()); merchant.setNormalizedName(normalized);
        merchant.setMerchantType(command.merchantType()); merchant.setCategory(category(command.categoryCode()));
    }

    @Transactional
    public void delete(String reference) {
        Merchant merchant = findMerchant(reference);
        if (transactions.countByMerchantId(merchant.getId()) > 0 || aliases.countByMerchantId(merchant.getId()) > 0)
            throw new MerchantDeletionNotAllowedException(referenceMapper.toReference(merchant.getId()));
        merchants.delete(merchant);
    }

    @Transactional
    public Long addAlias(String reference, KnowledgeBaseAliasCommand command) {
        Merchant merchant = findMerchant(reference); String normalized = normalized(command.aliasName());
        if (aliases.findByNormalizedAlias(normalized).isPresent()) throw new IllegalArgumentException("Alias already exists");
        return aliases.save(MerchantAlias.builder().aliasName(command.aliasName().trim()).normalizedAlias(normalized).merchant(merchant).verified(true).build()).getId();
    }

    @Transactional
    public void updateAlias(String reference, Long aliasId, KnowledgeBaseAliasCommand command) {
        MerchantAlias alias = aliases.findById(aliasId).filter(item -> item.getMerchant().getId().equals(findMerchant(reference).getId()))
                .orElseThrow(() -> new IllegalArgumentException("Alias was not found"));
        String normalized = normalized(command.aliasName());
        aliases.findByNormalizedAlias(normalized).filter(other -> !other.getId().equals(alias.getId()))
                .ifPresent(other -> { throw new IllegalArgumentException("Alias already exists"); });
        alias.setAliasName(command.aliasName().trim()); alias.setNormalizedAlias(normalized);
    }

    @Transactional
    public void deleteAlias(String reference, Long aliasId) {
        MerchantAlias alias = aliases.findById(aliasId).filter(item -> item.getMerchant().getId().equals(findMerchant(reference).getId()))
                .orElseThrow(() -> new IllegalArgumentException("Alias was not found"));
        aliases.delete(alias);
    }

    public List<Category> findCategories() { return categories.findAll().stream().filter(Category::isActive).toList(); }

    private boolean matches(Merchant merchant, String term) {
        return merchant.getOriginalName().toUpperCase().contains(term) || merchant.getNormalizedName().contains(term)
                || merchant.getAliases().stream().anyMatch(alias -> alias.getAliasName().toUpperCase().contains(term) || alias.getNormalizedAlias().contains(term));
    }
    private Merchant findMerchant(String reference) { MerchantReference ref = new MerchantReference(reference); return merchants.findById(referenceMapper.toMerchantId(ref)).orElseThrow(() -> new MerchantNotFoundException(ref)); }
    private Category category(String code) { return categories.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Category was not found")); }
    private String normalized(String value) { String result = normalizer.normalize(value).normalizedName(); if (result == null || result.isBlank()) throw new IllegalArgumentException("Name must not be blank"); return result; }
    private KnowledgeBaseMerchant toModel(Merchant merchant) {
        long count = transactions.countByMerchantId(merchant.getId()); BigDecimal total = transactions.sumAbsoluteAmountByMerchantId(merchant.getId());
        return new KnowledgeBaseMerchant(referenceMapper.toReference(merchant.getId()), merchant.getOriginalName(), merchant.getNormalizedName(), merchant.getMerchantType(),
                merchant.getCategory().getCode(), merchant.getCategory().getName(), count, total == null ? BigDecimal.ZERO : total,
                transactions.findFirstTransactionDateByMerchantId(merchant.getId()).orElse(null), transactions.findLastTransactionDateByMerchantId(merchant.getId()).orElse(null),
                aliases.findAllByMerchantIdOrderByAliasNameAsc(merchant.getId()).stream().map(alias -> new KnowledgeBaseAlias(alias.getId(), alias.getAliasName(), alias.getNormalizedAlias(), alias.isVerified())).toList());
    }
}
