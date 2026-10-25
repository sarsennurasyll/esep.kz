package com.esep.merchantmanagement.api;

import com.esep.merchantmanagement.exception.MerchantAliasAlreadyExistsException;
import com.esep.merchantmanagement.exception.MerchantDeletionNotAllowedException;
import com.esep.merchantmanagement.exception.MerchantNotFoundException;
import com.esep.merchantresolver.model.MerchantReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantApiErrorsTest {

    private final MerchantManagementExceptionHandler handler = new MerchantManagementExceptionHandler();

    @Test
    void shouldMatchStatusesOfHttpExceptionHandler() {
        var notFound = new MerchantNotFoundException(new MerchantReference("1"));
        var aliasExists = new MerchantAliasAlreadyExistsException("TEST STORE");
        var deletionNotAllowed = new MerchantDeletionNotAllowedException(new MerchantReference("2"));
        var invalid = new IllegalArgumentException("Name must not be blank");

        assertThat(MerchantApiErrors.statusOf(notFound)).isEqualTo(handler.handleMerchantNotFound(notFound).getStatusCode());
        assertThat(MerchantApiErrors.statusOf(aliasExists)).isEqualTo(handler.handleAliasAlreadyExists(aliasExists).getStatusCode());
        assertThat(MerchantApiErrors.statusOf(deletionNotAllowed))
                .isEqualTo(handler.handleMerchantDeletionNotAllowed(deletionNotAllowed).getStatusCode());
        assertThat(MerchantApiErrors.statusOf(invalid)).isEqualTo(handler.handleInvalidRequest(invalid).getStatusCode());
    }

    @Test
    void shouldHideMessageOfUnexpectedError() {
        assertThat(MerchantApiErrors.clientMessageOf(new IllegalStateException("Тестов Т."))).isEqualTo(MerchantApiErrors.INTERNAL_ERROR_MESSAGE);
    }
}
