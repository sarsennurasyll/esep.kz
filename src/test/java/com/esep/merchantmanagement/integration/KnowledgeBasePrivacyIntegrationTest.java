package com.esep.merchantmanagement.integration;

import com.esep.entity.Category;
import com.esep.entity.DetectionSource;
import com.esep.entity.Merchant;
import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.exception.MerchantVersionConflictException;
import com.esep.merchantmanagement.interfaces.MerchantAliasMatchCatalog;
import com.esep.merchantmanagement.model.KnowledgeBaseAliasCommand;
import com.esep.merchantmanagement.model.KnowledgeBaseMerchantCommand;
import com.esep.merchantmanagement.model.MerchantAliasMatchCommand;
import com.esep.merchantmanagement.service.KnowledgeBaseMerchantService;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.normalization.service.DefaultMerchantNormalizer;
import com.esep.persistence.jpa.adapter.MerchantAliasMatchCatalogJpaAdapter;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({
        KnowledgeBaseMerchantService.class,
        MerchantAliasMatchCatalogJpaAdapter.class,
        MerchantReferenceJpaMapper.class,
        DefaultMerchantNormalizer.class
})
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class KnowledgeBasePrivacyIntegrationTest {

    @Autowired
    private KnowledgeBaseMerchantService knowledgeBaseMerchantService;

    @Autowired
    private MerchantAliasMatchCatalog merchantAliasMatchCatalog;

    @Autowired
    private EntityManager entityManager;

    private Merchant systemPersonTransfers;
    private Merchant store;

    @BeforeEach
    void setUp() {
        Category transfers = Category.builder().code("PERSONAL_TRANSFERS").name("Переводы людям").active(true).build();
        Category grocery = Category.builder().code("GROCERY").name("Продукты").active(true).build();
        entityManager.persist(transfers);
        entityManager.persist(grocery);
        systemPersonTransfers = merchant("Переводы людям", "ПЕРЕВОДЫ ЛЮДЯМ", MerchantType.PERSON, transfers);
        store = merchant("TEST STORE", "TEST STORE", MerchantType.STORE, grocery);
        entityManager.flush();
    }

    @Test
    void shouldRejectUpdateBasedOnOutdatedVersion() {
        String reference = String.valueOf(store.getId());
        long version = knowledgeBaseMerchantService.findById(reference).version();
        knowledgeBaseMerchantService.update(reference, new KnowledgeBaseMerchantCommand("TEST STORE NEW", MerchantType.STORE, "GROCERY", version));
        entityManager.flush();

        assertThatThrownBy(() -> knowledgeBaseMerchantService.update(reference,
                new KnowledgeBaseMerchantCommand("TEST STORE OLD", MerchantType.STORE, "GROCERY", version)))
                .isInstanceOf(MerchantVersionConflictException.class);
        assertThat(knowledgeBaseMerchantService.findById(reference).name()).isEqualTo("TEST STORE NEW");
    }

    @Test
    void shouldRejectCreatingPersonInSharedKnowledgeBase() {
        assertThatThrownBy(() -> knowledgeBaseMerchantService.create(
                new KnowledgeBaseMerchantCommand("Тестов Т.", MerchantType.PERSON, "PERSONAL_TRANSFERS")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAcceptTradeNameEntrepreneurButRejectEntrepreneurNamedAfterPerson() {
        MerchantReference gamer = knowledgeBaseMerchantService.create(
                new KnowledgeBaseMerchantCommand("ИП Gamer", MerchantType.STORE, "GROCERY"));

        assertThat(knowledgeBaseMerchantService.findById(gamer.value()).name()).isEqualTo("ИП Gamer");
        assertThatThrownBy(() -> knowledgeBaseMerchantService.create(
                new KnowledgeBaseMerchantCommand("ИП Тестов Т.", MerchantType.STORE, "GROCERY")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> knowledgeBaseMerchantService.addAlias(reference(store), new KnowledgeBaseAliasCommand("ИП Тестова")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> knowledgeBaseMerchantService.update(reference(store),
                new KnowledgeBaseMerchantCommand("Тестов Т.", MerchantType.STORE, "GROCERY")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectTurningOrganizationIntoPerson() {
        assertThatThrownBy(() -> knowledgeBaseMerchantService.update(
                reference(store),
                new KnowledgeBaseMerchantCommand("TEST STORE", MerchantType.PERSON, "PERSONAL_TRANSFERS")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldKeepSystemPersonTransfersEditable() {
        knowledgeBaseMerchantService.update(
                reference(systemPersonTransfers),
                new KnowledgeBaseMerchantCommand("Переводы людям", MerchantType.PERSON, "PERSONAL_TRANSFERS")
        );

        assertThat(knowledgeBaseMerchantService.findById(reference(systemPersonTransfers)).merchantType())
                .isEqualTo(MerchantType.PERSON);
    }

    @Test
    void shouldRejectAliasForPerson() {
        assertThatThrownBy(() -> knowledgeBaseMerchantService.addAlias(
                reference(systemPersonTransfers),
                new KnowledgeBaseAliasCommand("Тестов Т.")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectConfirmedMatchToPerson() {
        assertThatThrownBy(() -> merchantAliasMatchCatalog.save(new MerchantAliasMatchCommand(
                "ТЕСТОВ Т",
                "ТЕСТОВ Т",
                new MerchantReference(reference(systemPersonTransfers))
        ))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldStillAcceptAliasForOrganization() {
        Long aliasId = knowledgeBaseMerchantService.addAlias(reference(store), new KnowledgeBaseAliasCommand("TEST STORE 2"));

        assertThat(aliasId).isNotNull();
    }

    private Merchant merchant(String originalName, String normalizedName, MerchantType type, Category category) {
        Merchant merchant = Merchant.builder().originalName(originalName).normalizedName(normalizedName)
                .merchantType(type).category(category).confidence(BigDecimal.ONE)
                .detectionSource(DetectionSource.DATABASE).verified(true).build();
        entityManager.persist(merchant);
        return merchant;
    }

    private String reference(Merchant merchant) {
        return merchant.getId().toString();
    }
}
