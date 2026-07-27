package com.esep.persistence.jpa.adapter;

import com.esep.entity.BankType;
import com.esep.entity.Category;
import com.esep.entity.Merchant;
import com.esep.entity.MerchantAlias;
import com.esep.entity.Statement;
import com.esep.entity.TransactionType;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.persistence.jpa.mapper.MerchantAliasRecordJpaMapper;
import com.esep.persistence.jpa.mapper.MerchantRecordJpaMapper;
import com.esep.persistence.jpa.mapper.MerchantReferenceJpaMapper;
import com.esep.persistence.jpa.mapper.StatementJpaMapper;
import com.esep.persistence.jpa.mapper.TransactionJpaMapper;
import com.esep.persistence.jpa.repository.CategoryJpaRepository;
import com.esep.persistence.jpa.repository.MerchantAliasJpaRepository;
import com.esep.persistence.jpa.repository.MerchantJpaRepository;
import com.esep.persistence.jpa.repository.StatementJpaRepository;
import com.esep.persistence.jpa.repository.TransactionJpaRepository;
import com.esep.persistence.model.StatementPersistenceCommand;
import com.esep.persistence.model.TransactionPersistenceCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaAdaptersTest {

    @Test
    void shouldSaveStatementThroughJpaRepository() {
        StatementJpaRepository repository = mock(StatementJpaRepository.class);
        StatementCatalogJpaAdapter adapter = new StatementCatalogJpaAdapter(repository, new StatementJpaMapper());
        StatementPersistenceCommand command = statementCommand();

        adapter.save(command);

        verify(repository).save(any(Statement.class));
        when(repository.existsBySourceFileHash(command.sourceFileHash())).thenReturn(true);
        assertThat(adapter.existsBySourceFileHash(command.sourceFileHash())).isTrue();
    }

    @Test
    void shouldSaveTransactionsWithResolvedMerchant() {
        TransactionJpaRepository transactionRepository = mock(TransactionJpaRepository.class);
        StatementJpaRepository statementRepository = mock(StatementJpaRepository.class);
        MerchantJpaRepository merchantRepository = mock(MerchantJpaRepository.class);
        Statement statement = statementEntity();
        Merchant merchant = merchantEntity(1L);
        TransactionCatalogJpaAdapter adapter = new TransactionCatalogJpaAdapter(
                transactionRepository,
                statementRepository,
                merchantRepository,
                new TransactionJpaMapper(),
                new MerchantReferenceJpaMapper()
        );
        TransactionPersistenceCommand command = new TransactionPersistenceCommand(
                LocalDate.of(2026, 7, 12),
                "MAGNUM",
                new BigDecimal("14500.00"),
                "KZT",
                TransactionType.EXPENSE,
                new MerchantReference("1"),
                "b".repeat(64)
        );

        when(statementRepository.findBySourceFileHash("a".repeat(64))).thenReturn(Optional.of(statement));
        when(merchantRepository.findById(1L)).thenReturn(Optional.of(merchant));

        adapter.saveAll("a".repeat(64), List.of(command));

        verify(transactionRepository).saveAll(any());
    }

    @Test
    void shouldFindCategoryNameByCode() {
        CategoryJpaRepository repository = mock(CategoryJpaRepository.class);
        CategoryCatalogJpaAdapter adapter = new CategoryCatalogJpaAdapter(repository);
        Category category = Category.builder().code("GROCERY").name("Продукты").build();

        when(repository.findByCode("GROCERY")).thenReturn(Optional.of(category));

        assertThat(adapter.findNameByCode("GROCERY")).contains("Продукты");
    }

    @Test
    void shouldMapMerchantEntityToExistingCatalogContract() {
        MerchantJpaRepository repository = mock(MerchantJpaRepository.class);
        MerchantReferenceJpaMapper referenceMapper = new MerchantReferenceJpaMapper();
        MerchantCatalogJpaAdapter adapter = new MerchantCatalogJpaAdapter(
                repository,
                new MerchantRecordJpaMapper(referenceMapper),
                referenceMapper
        );
        Merchant merchant = merchantEntity(1L);

        when(repository.findByNormalizedName("MAGNUM")).thenReturn(Optional.of(merchant));

        assertThat(adapter.findByCanonicalName("MAGNUM"))
                .hasValueSatisfying(record -> assertThat(record.merchantReference().value()).isEqualTo("1"));
    }

    @Test
    void shouldMapMerchantAliasVerificationToExistingCatalogContract() {
        MerchantAliasJpaRepository repository = mock(MerchantAliasJpaRepository.class);
        MerchantReferenceJpaMapper referenceMapper = new MerchantReferenceJpaMapper();
        MerchantAliasCatalogJpaAdapter adapter = new MerchantAliasCatalogJpaAdapter(
                repository,
                new MerchantAliasRecordJpaMapper(referenceMapper)
        );
        MerchantAlias alias = MerchantAlias.builder()
                .aliasName("MAGNUM CC")
                .normalizedAlias("MAGNUM CC")
                .merchant(merchantEntity(1L))
                .verified(true)
                .build();
        alias.setId(10L);

        when(repository.findByNormalizedAlias("MAGNUM CC")).thenReturn(Optional.of(alias));

        assertThat(adapter.findByNormalizedAlias("MAGNUM CC"))
                .hasValueSatisfying(record -> assertThat(record.verified()).isTrue());
    }

    private StatementPersistenceCommand statementCommand() {
        return new StatementPersistenceCommand(
                BankType.KASPI,
                "statement.pdf",
                "****1234",
                "a".repeat(64),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
    }

    private Statement statementEntity() {
        return Statement.builder()
                .bankName(BankType.KASPI)
                .originalFileName("statement.pdf")
                .sourceFileHash("a".repeat(64))
                .periodFrom(LocalDate.of(2026, 7, 1))
                .periodTo(LocalDate.of(2026, 7, 31))
                .build();
    }

    private Merchant merchantEntity(Long id) {
        Merchant merchant = Merchant.builder()
                .originalName("MAGNUM")
                .normalizedName("MAGNUM")
                .build();
        merchant.setId(id);
        return merchant;
    }
}
