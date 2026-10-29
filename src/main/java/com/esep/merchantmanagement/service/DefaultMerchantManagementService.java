package com.esep.merchantmanagement.service;

import com.esep.merchantmanagement.exception.MerchantAliasAlreadyExistsException;
import com.esep.merchantmanagement.exception.MerchantNotFoundException;
import com.esep.merchantmanagement.interfaces.MerchantAliasMatchCatalog;
import com.esep.merchantmanagement.interfaces.MerchantManagementService;
import com.esep.merchantmanagement.interfaces.MerchantReadQuery;
import com.esep.merchantmanagement.interfaces.MerchantTransactionBindingCatalog;
import com.esep.merchantmanagement.interfaces.UnknownMerchantDescriptionQuery;
import com.esep.merchantmanagement.model.MerchantAliasMatchCommand;
import com.esep.merchantmanagement.policy.PersonalNameDetector;
import com.esep.merchantmanagement.model.MerchantLearningStatistics;
import com.esep.merchantmanagement.model.MerchantSuggestion;
import com.esep.merchantmanagement.model.MerchantSummary;
import com.esep.merchantmanagement.model.UnknownMerchantCandidate;
import com.esep.merchantmanagement.model.UnknownMerchantDescription;
import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.normalization.interfaces.MerchantNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Каталог продавцов загружается один раз на весь список, а не для каждого описания:
     * подсказка ищется по всем продавцам и алиасам, и повторная загрузка давала O(описаний × каталог) запросов.
     */
    @Override
    public List<UnknownMerchantDescription> findUnknownDescriptions() {
        Map<String, List<UnknownMerchantCandidate>> candidatesByDescription = new LinkedHashMap<>();
        Map<String, Boolean> knownDescriptions = new HashMap<>();

        for (UnknownMerchantCandidate candidate : unknownMerchantDescriptionQuery.findAll()) {
            // Имя человека (в том числе «ИП Фамилия И.») не предлагается для общей базы: связать его с продавцом нельзя.
            if (PersonalNameDetector.looksLikePersonalName(candidate.description())) {
                continue;
            }
            String normalizedDescription = merchantNormalizer.normalize(candidate.description()).normalizedName();
            if (normalizedDescription == null || normalizedDescription.isBlank()
                    || knownDescriptions.computeIfAbsent(normalizedDescription, this::isKnown)) {
                continue;
            }
            candidatesByDescription.computeIfAbsent(normalizedDescription, ignored -> new ArrayList<>()).add(candidate);
        }

        List<MerchantSummary> merchants = candidatesByDescription.isEmpty() ? List.of() : merchantReadQuery.findAll();
        return candidatesByDescription.entrySet().stream()
                .map(entry -> toUnknownDescription(entry.getKey(), entry.getValue(), suggestion(entry.getKey(), merchants)))
                .sorted(Comparator.comparingLong(UnknownMerchantDescription::usageCount).reversed()
                        .thenComparing(UnknownMerchantDescription::totalAmount, Comparator.reverseOrder())
                        .thenComparing(UnknownMerchantDescription::lastTransactionDate, Comparator.reverseOrder()))
                .toList();
    }

    private static UnknownMerchantDescription toUnknownDescription(
            String normalizedDescription,
            List<UnknownMerchantCandidate> candidates,
            MerchantSuggestion suggestion
    ) {
        return new UnknownMerchantDescription(
                normalizedDescription,
                candidates.stream().mapToLong(UnknownMerchantCandidate::usageCount).sum(),
                candidates.stream().map(UnknownMerchantCandidate::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                candidates.stream().map(UnknownMerchantCandidate::lastTransactionDate).max(Comparator.naturalOrder()).orElseThrow(),
                candidates.getFirst().description(),
                candidates.stream().allMatch(UnknownMerchantCandidate::newInLatestStatement),
                suggestion
        );
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

        List<String> matchingDescriptions = unknownMerchantDescriptionQuery.findAll().stream()
                .map(UnknownMerchantCandidate::description)
                .filter(description -> normalizedAlias.equals(merchantNormalizer.normalize(description).normalizedName()))
                .distinct()
                .toList();
        // Нормализация убирает «ИП», поэтому имя человека проверяется и по исходным описаниям операций.
        if (PersonalNameDetector.looksLikePersonalName(normalizedDescription)
                || matchingDescriptions.stream().anyMatch(PersonalNameDetector::looksLikePersonalName)) {
            throw new IllegalArgumentException("Private persons are not stored in the shared merchant knowledge base");
        }

        merchantAliasMatchCatalog.save(new MerchantAliasMatchCommand(
                normalizedAlias,
                normalizedAlias,
                merchantReference
        ));
        merchantTransactionBindingCatalog.bindUnknownTransactions(matchingDescriptions, merchantReference);
    }

    private boolean isKnown(String normalizedDescription) {
        return merchantCatalog.findByCanonicalName(normalizedDescription).isPresent()
                || merchantAliasCatalog.findByNormalizedAlias(normalizedDescription).isPresent();
    }

    private MerchantSuggestion suggestion(String normalizedDescription, List<MerchantSummary> merchants) {
        return merchants.stream()
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
