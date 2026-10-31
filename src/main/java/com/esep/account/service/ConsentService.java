package com.esep.account.service;

import com.esep.entity.LegalDocument;
import com.esep.entity.UserConsent;
import com.esep.persistence.jpa.repository.UserConsentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Журнал согласий с версиями документов (ADR-0010).
 */
@Service
public class ConsentService {

    private final UserConsentJpaRepository consents;

    public ConsentService(UserConsentJpaRepository consents) {
        this.consents = consents;
    }

    /** Обязательные документы, текущую версию которых пользователь ещё не принял. */
    @Transactional(readOnly = true)
    public List<LegalDocument> outstanding(long accountId) {
        Map<LegalDocument, UserConsent> latest = latest(accountId);
        return Arrays.stream(LegalDocument.values())
                .filter(LegalDocument::required)
                .filter(document -> !isGrantedCurrentVersion(latest.get(document), document))
                .toList();
    }

    @Transactional
    public void acceptRequired(long accountId) {
        outstanding(accountId).forEach(document -> record(accountId, document, true));
    }

    /** Согласие считается только на текущую версию: новый текст требует нового согласия. */
    @Transactional(readOnly = true)
    public boolean isContributing(long accountId) {
        return isGrantedCurrentVersion(latest(accountId).get(LegalDocument.MERCHANT_CONTRIBUTION), LegalDocument.MERCHANT_CONTRIBUTION);
    }

    @Transactional
    public void setContribution(long accountId, boolean granted) {
        record(accountId, LegalDocument.MERCHANT_CONTRIBUTION, granted);
    }

    @Transactional(readOnly = true)
    public Map<LegalDocument, UserConsent> latest(long accountId) {
        Map<LegalDocument, UserConsent> latest = new EnumMap<>(LegalDocument.class);
        consents.findByAccountIdOrderByIdDesc(accountId).forEach(consent -> latest.putIfAbsent(consent.getDocument(), consent));
        return latest;
    }

    private void record(long accountId, LegalDocument document, boolean granted) {
        consents.save(UserConsent.builder().accountId(accountId).document(document).version(document.version()).granted(granted).build());
    }

    private static boolean isGrantedCurrentVersion(UserConsent consent, LegalDocument document) {
        return consent != null && consent.isGranted() && consent.getVersion().equals(document.version());
    }
}
