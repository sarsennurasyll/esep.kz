package com.esep.merchantmanagement.service;

import com.esep.merchantmanagement.exception.MerchantAliasAlreadyExistsException;
import com.esep.merchantmanagement.exception.MerchantNotFoundException;
import com.esep.merchantmanagement.interfaces.MerchantAliasMatchCatalog;
import com.esep.merchantmanagement.interfaces.MerchantManagementService;
import com.esep.merchantmanagement.interfaces.MerchantReadQuery;
import com.esep.merchantmanagement.interfaces.MerchantTransactionBindingCatalog;
import com.esep.merchantmanagement.interfaces.UnknownMerchantDescriptionQuery;
import com.esep.merchantmanagement.model.MerchantAliasMatchCommand;
import com.esep.merchantmanagement.model.MerchantLearningStatistics;
import com.esep.merchantmanagement.model.MerchantSuggestion;
import com.esep.merchantmanagement.model.MerchantSummary;
import com.esep.merchantmanagement.model.UnknownMerchantCandidate;
import com.esep.merchantmanagement.model.UnknownMerchantDescription;
import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.normalization.interfaces.MerchantNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Реализует прикладной сценарий подтверждения неизвестных описаний операций.
 */
@Service
public class DefaultMerchantManagementService implements MerchantManagementService {

    private final UnknownMerchantDescriptionQuery unknownMerchantDescriptionQuery;
    private final MerchantReadQuery merchantReadQuery;
    private final MerchantCatalog merchantCatalog;
    private final MerchantAliasCatalog merchantAliasCatalog;
    private final MerchantAliasMatchCatalog merchantAliasMatchCatalog;
    private final MerchantTransactionBindingCatalog merchantTransactionBindingCatalog;
    private final MerchantNormalizer merchantNormalizer;

    @Autowired
    public DefaultMerchantManagementService(
            UnknownMerchantDescriptionQuery unknownMerchantDescriptionQuery,
            MerchantReadQuery merchantReadQuery,
            MerchantCatalog merchantCatalog,
            MerchantAliasCatalog merchantAliasCatalog,
            MerchantAliasMatchCatalog merchantAliasMatchCatalog,
            MerchantTransactionBindingCatalog merchantTransactionBindingCatalog,
            MerchantNormalizer merchantNormalizer
    ) {
        this.unknownMerchantDescriptionQuery = unknownMerchantDescriptionQuery;
        this.merchantReadQuery = merchantReadQuery;
        this.merchantCatalog = merchantCatalog;
        this.merchantAliasCatalog = merchantAliasCatalog;
        this.merchantAliasMatchCatalog = merchantAliasMatchCatalog;
        this.merchantTransactionBindingCatalog = merchantTransactionBindingCatalog;
        this.merchantNormalizer = merchantNormalizer;
    }

    /**
     * Сохраняет совместимость с автономными in-memory тестами распознавания.
     */
    public DefaultMerchantManagementService(
            UnknownMerchantDescriptionQuery unknownMerchantDescriptionQuery,
            MerchantReadQuery merchantReadQuery,
            MerchantCatalog merchantCatalog,
            MerchantAliasCatalog merchantAliasCatalog,
            MerchantAliasMatchCatalog merchantAliasMatchCatalog,
            MerchantNormalizer merchantNormalizer
    ) {
        this(
                unknownMerchantDescriptionQuery,
                merchantReadQuery,
                merchantCatalog,
                merchantAliasCatalog,
                merchantAliasMatchCatalog,
                (descriptions, merchantReference) -> 0,
                merchantNormalizer
        );
    }

    @Override
    public List<UnknownMerchantDescription> findUnknownDescriptions() {
        Map<String, UnknownMerchantDescription> descriptions = new LinkedHashMap<>();

        for (UnknownMerchantCandidate candidate : unknownMerchantDescriptionQuery.findAll()) {
            String normalizedDescription = merchantNormalizer.normalize(candidate.description()).normalizedName();
            if (normalizedDescription == null || normalizedDescription.isBlank() || isKnown(normalizedDescription)) {
                continue;
            }

            descriptions.merge(
                    normalizedDescription,
                    new UnknownMerchantDescription(normalizedDescription, candidate.usageCount(), candidate.totalAmount(),
                            candidate.lastTransactionDate(), candidate.description(), candidate.newInLatestStatement(), suggestion(normalizedDescription)),
                    (current, next) -> new UnknownMerchantDescription(
                            current.normalizedDescription(),
                            current.usageCount() + next.usageCount(),
                            current.totalAmount().add(next.totalAmount()),
                            current.lastTransactionDate().isAfter(next.lastTransactionDate()) ? current.lastTransactionDate() : next.lastTransactionDate(),
                            current.exampleDescription(),
                            current.newInLatestStatement() && next.newInLatestStatement(),
                            current.suggestion()
                    )
            );
        }

        return descriptions.values().stream()
                .sorted(java.util.Comparator.comparingLong(UnknownMerchantDescription::usageCount).reversed()
                        .thenComparing(UnknownMerchantDescription::totalAmount, java.util.Comparator.reverseOrder())
                        .thenComparing(UnknownMerchantDescription::lastTransactionDate, java.util.Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public List<MerchantSummary> findMerchants() {
        return merchantReadQuery.findAll();
    }

    @Override
    @Transactional
    public void match(String normalizedDescription, MerchantReference merchantReference) {
        String normalizedAlias = merchantNormalizer.normalize(normalizedDescription).normalizedName();
        if (normalizedAlias == null || normalizedAlias.isBlank()) {
            throw new IllegalArgumentException("Normalized description must not be blank");
        }

        merchantCatalog.findByReference(merchantReference)
                .orElseThrow(() -> new MerchantNotFoundException(merchantReference));

        if (merchantAliasCatalog.findByNormalizedAlias(normalizedAlias).isPresent()) {
            throw new MerchantAliasAlreadyExistsException(normalizedAlias);
        }

        merchantAliasMatchCatalog.save(new MerchantAliasMatchCommand(
                normalizedAlias,
                normalizedAlias,
                merchantReference
        ));

        List<String> matchingDescriptions = unknownMerchantDescriptionQuery.findAll().stream()
                .map(UnknownMerchantCandidate::description)
                .filter(description -> normalizedAlias.equals(merchantNormalizer.normalize(description).normalizedName()))
                .distinct()
                .toList();
        merchantTransactionBindingCatalog.bindUnknownTransactions(matchingDescriptions, merchantReference);
    }

    private boolean isKnown(String normalizedDescription) {
        return merchantCatalog.findByCanonicalName(normalizedDescription).isPresent()
                || merchantAliasCatalog.findByNormalizedAlias(normalizedDescription).isPresent();
    }

    private MerchantSuggestion suggestion(String normalizedDescription) {
        return merchantReadQuery.findAll().stream()
                .filter(merchant -> sharesMeaningfulToken(normalizedDescription, merchant.displayName())
                        || merchant.aliases().stream().anyMatch(alias -> sharesMeaningfulToken(normalizedDescription, alias)))
                .findFirst()
                .map(merchant -> new MerchantSuggestion(merchant.merchantReference().value(), merchant.displayName(), merchant.categoryName()))
                .orElse(null);
    }

    private boolean sharesMeaningfulToken(String description, String merchantName) {
        for (String descriptionToken : description.split(" ")) {
            if (descriptionToken.length() < 4) {
                continue;
            }
            for (String merchantToken : merchantName.toUpperCase().split(" ")) {
                if (descriptionToken.equals(merchantToken)) {
                    return true;
                }
            }
        }
        return false;
    }
}
