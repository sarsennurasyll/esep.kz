package com.esep.merchantresolver.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MerchantReferenceTest {

    @Test
    void shouldKeepReferenceValueOpaqueToConsumers() {
        MerchantReference reference = new MerchantReference("merchant-001");

        assertThat(reference.value()).isEqualTo("merchant-001");
    }

    @Test
    void shouldRejectBlankReference() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MerchantReference("  "));
    }

    @Test
    void shouldRejectNullReference() {
        assertThatNullPointerException()
                .isThrownBy(() -> new MerchantReference(null));
    }
}
