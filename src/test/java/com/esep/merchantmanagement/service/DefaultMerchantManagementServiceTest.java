package com.esep.merchantmanagement.service;

import com.esep.merchantmanagement.exception.MerchantAliasAlreadyExistsException;
import com.esep.merchantmanagement.interfaces.MerchantAliasMatchCatalog;
import com.esep.merchantmanagement.interfaces.MerchantReadQuery;
import com.esep.merchantmanagement.interfaces.MerchantTransactionBindingCatalog;
import com.esep.merchantmanagement.interfaces.UnknownMerchantDescriptionQuery;
import com.esep.merchantmanagement.model.MerchantAliasMatchCommand;
import com.esep.merchantmanagement.model.UnknownMerchantCandidate;
import com.esep.merchantmanagement.model.UnknownMerchantDescription;
import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.model.MerchantRecord;
import com.esep.merchantresolver.model.MerchantReference;
import com.esep.normalization.service.DefaultMerchantNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultMerchantManagementServiceTest {

    @Test
    void shouldNormalizeAndAggregateUnknownDescriptions() {
        UnknownMerchantDescriptionQuery unknownQuery = () -> List.of(
                new UnknownMerchantCandidate(" Unknown Shop ", 2),
                new UnknownMerchantCandidate("unknown   shop", 3),
                new UnknownMerchantCandidate("MAGNUM", 1)
        );
        MerchantCatalog merchantCatalog = mock(MerchantCatalog.class);
        MerchantAliasCatalog aliasCatalog = mock(MerchantAliasCatalog.class);
        when(merchantCatalog.findByCanonicalName("MAGNUM"))
                .thenReturn(Optional.of(new MerchantRecord(new MerchantReference("1"), "MAGNUM")));
        when(merchantCatalog.findByCanonicalName("UNKNOWN SHOP")).thenReturn(Optional.empty());
        when(aliasCatalog.findByNormalizedAlias("UNKNOWN SHOP")).thenReturn(Optional.empty());

        DefaultMerchantManagementService service = service(unknownQuery, merchantCatalog, aliasCatalog, command -> { });

        assertThat(service.findUnknownDescriptions()).containsExactly(
                new UnknownMerchantDescription("UNKNOWN SHOP", 5, " Unknown Shop ")
        );
    }

    @Test
    void shouldSaveVerifiedAliasForExistingMerchant() {
        MerchantReference merchantReference = new MerchantReference("42");
        MerchantCatalog merchantCatalog = mock(MerchantCatalog.class);
        MerchantAliasCatalog aliasCatalog = mock(MerchantAliasCatalog.class);
        MerchantAliasMatchCatalog commandCatalog = mock(MerchantAliasMatchCatalog.class);
        when(merchantCatalog.findByReference(merchantReference))
                .thenReturn(Optional.of(new MerchantRecord(merchantReference, "MAGNUM")));
        when(aliasCatalog.findByNormalizedAlias("UNKNOWN SHOP")).thenReturn(Optional.empty());

        MerchantTransactionBindingCatalog bindingCatalog = mock(MerchantTransactionBindingCatalog.class);
        DefaultMerchantManagementService service = service(
                () -> List.of(new UnknownMerchantCandidate(" unknown shop ", 2)),
                merchantCatalog, aliasCatalog, commandCatalog, bindingCatalog
        );
        service.match(" unknown shop ", merchantReference);

        verify(commandCatalog).save(new MerchantAliasMatchCommand(
                "UNKNOWN SHOP",
                "UNKNOWN SHOP",
                merchantReference
        ));
        verify(bindingCatalog).bindUnknownTransactions(List.of(" unknown shop "), merchantReference);
    }

    @Test
    void shouldRejectExistingAlias() {
        MerchantReference merchantReference = new MerchantReference("42");
        MerchantCatalog merchantCatalog = mock(MerchantCatalog.class);
        MerchantAliasCatalog aliasCatalog = mock(MerchantAliasCatalog.class);
        when(merchantCatalog.findByReference(merchantReference))
                .thenReturn(Optional.of(new MerchantRecord(merchantReference, "MAGNUM")));
        when(aliasCatalog.findByNormalizedAlias("UNKNOWN SHOP"))
                .thenReturn(Optional.of(mock(com.esep.merchantresolver.model.MerchantAliasRecord.class)));

        DefaultMerchantManagementService service = service(() -> List.of(), merchantCatalog, aliasCatalog, command -> { });

        assertThatThrownBy(() -> service.match("UNKNOWN SHOP", merchantReference))
                .isInstanceOf(MerchantAliasAlreadyExistsException.class);
    }

    private DefaultMerchantManagementService service(
            UnknownMerchantDescriptionQuery unknownQuery,
            MerchantCatalog merchantCatalog,
            MerchantAliasCatalog aliasCatalog,
            MerchantAliasMatchCatalog commandCatalog
    ) {
        MerchantReadQuery merchantReadQuery = List::of;
        return new DefaultMerchantManagementService(
                unknownQuery,
                merchantReadQuery,
                merchantCatalog,
                aliasCatalog,
                commandCatalog,
                new DefaultMerchantNormalizer()
        );
    }

    private DefaultMerchantManagementService service(
            UnknownMerchantDescriptionQuery unknownQuery,
            MerchantCatalog merchantCatalog,
            MerchantAliasCatalog aliasCatalog,
            MerchantAliasMatchCatalog commandCatalog,
            MerchantTransactionBindingCatalog bindingCatalog
    ) {
        return new DefaultMerchantManagementService(
                unknownQuery,
                List::of,
                merchantCatalog,
                aliasCatalog,
                commandCatalog,
                bindingCatalog,
                new DefaultMerchantNormalizer()
        );
    }
}
