package com.esep.merchantmanagement.web;

import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.api.KnowledgeBaseMerchantController;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantRequest;
import com.esep.merchantmanagement.exception.MerchantDeletionNotAllowedException;
import com.esep.merchantmanagement.exception.MerchantNotFoundException;
import com.esep.merchantresolver.model.MerchantReference;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InProcessKnowledgeBaseWebApiClientTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final KnowledgeBaseMerchantController controller = mock(KnowledgeBaseMerchantController.class);
    private final InProcessKnowledgeBaseWebApiClient client = new InProcessKnowledgeBaseWebApiClient(controller, validator);

    @Test
    void shouldRejectInvalidRequestBeforeCallingApi() {
        assertThatThrownBy(() -> client.createMerchant(new KnowledgeBaseMerchantRequest("", MerchantType.STORE, "GROCERY")))
                .isInstanceOfSatisfying(MerchantApiException.class, exception -> assertThat(exception.getStatusCode()).isEqualTo(400));
        verifyNoInteractions(controller);
    }

    @Test
    void shouldMapNotFoundAndConflict() {
        when(controller.findMerchant("1")).thenThrow(new MerchantNotFoundException(new MerchantReference("1")));
        doThrow(new MerchantDeletionNotAllowedException(new MerchantReference("2"))).when(controller).deleteMerchant("2");

        assertThatThrownBy(() -> client.findMerchant("1"))
                .isInstanceOfSatisfying(MerchantApiException.class, exception -> assertThat(exception.getStatusCode()).isEqualTo(404));
        assertThatThrownBy(() -> client.deleteMerchant("2"))
                .isInstanceOfSatisfying(MerchantApiException.class, exception -> assertThat(exception.getStatusCode()).isEqualTo(409));
    }
}
