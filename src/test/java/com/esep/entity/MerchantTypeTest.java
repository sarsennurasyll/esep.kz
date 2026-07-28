package com.esep.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantTypeTest {

    @Test
    void shouldExposeReadableNameForPersonTransfers() {
        assertThat(MerchantType.PERSON.displayName()).isEqualTo("Перевод человеку");
    }
}
