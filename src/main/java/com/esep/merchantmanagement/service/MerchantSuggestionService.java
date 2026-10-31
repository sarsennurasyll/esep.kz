package com.esep.merchantmanagement.service;

import com.esep.account.service.ConsentService;
import com.esep.entity.BankOperationType;
import com.esep.entity.MerchantSuggestion;
import com.esep.merchantmanagement.policy.PersonalNameDetector;
import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.persistence.jpa.repository.MerchantAliasJpaRepository;
import com.esep.persistence.jpa.repository.MerchantJpaRepository;
import com.esep.persistence.jpa.repository.MerchantSuggestionJpaRepository;
import com.esep.persistence.jpa.repository.TransactionJpaRepository;
import com.esep.security.AccountPrincipal;
import com.esep.statementimport.interfaces.StatementImportListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Пополнение общей базы по отдельному согласию (ADR-0010). Передаётся только нормализованное название покупки:
 * без сумм, дат и связи с человеком. Переводы не участвуют, имена людей отсекаются, а в базу название попадает
 * только после проверки администратором.
 */
@Service
public class MerchantSuggestionService implements StatementImportListener {

    private final ConsentService consentService;
    private final TransactionJpaRepository transactions;
    private final MerchantSuggestionJpaRepository suggestions;
    private final MerchantJpaRepository merchants;
    private final MerchantAliasJpaRepository aliases;
    private final MerchantNormalizer normalizer;

    public MerchantSuggestionService(
            ConsentService consentService,
            TransactionJpaRepository transactions,
            MerchantSuggestionJpaRepository suggestions,
            MerchantJpaRepository merchants,
            MerchantAliasJpaRepository aliases,
            MerchantNormalizer normalizer
    ) {
        this.consentService = consentService;
        this.transactions = transactions;
        this.suggestions = suggestions;
        this.merchants = merchants;
        this.aliases = aliases;
        this.normalizer = normalizer;
    }

    @Override
    @Transactional
    public void statementImported() {
        AccountPrincipal.current().ifPresent(principal -> contribute(principal.accountId()));
    }

    /** Ничего не делает без действующего согласия. Операции читаются только текущего владельца. */
    @Transactional
    public void contribute(long accountId) {
        if (!consentService.isContributing(accountId)) {
            return;
        }
        for (String description : transactions.findUnknownDescriptions(BankOperationType.PURCHASE)) {
            if (PersonalNameDetector.looksLikePersonalName(description)) {
                continue;
            }
            String name = normalizer.normalize(description).normalizedName();
            if (name != null && !name.isBlank() && !isKnown(name)) {
                suggestions.addIfAbsent(name);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<String> findNew() {
        return suggestions.findByStatusOrderByNormalizedName(MerchantSuggestion.STATUS_NEW).stream()
                .map(MerchantSuggestion::getNormalizedName)
                .filter(name -> !isKnown(name))
                .toList();
    }

    @Transactional
    public void reject(String normalizedName) {
        suggestions.reject(normalizedName);
    }

    private boolean isKnown(String name) {
        return merchants.findByNormalizedName(name).isPresent() || aliases.findByNormalizedAlias(name).isPresent();
    }
}
