package com.esep.merchantresolver.service;

import com.esep.merchantresolver.repository.InMemoryMerchantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryMerchantResolverTest {

    private final InMemoryMerchantResolver merchantResolver = new InMemoryMerchantResolver(
            new InMemoryMerchantRepository()
    );

    @ParameterizedTest
    @CsvSource({
            "MAGNUM, MAGNUM",
            "'MAGNUM CASH&CARRY', MAGNUM",
            "'YANDEX GO', 'YANDEX GO'",
            "ACTIV, ACTIV",
            "TELE2, TELE2"
    })
    void shouldResolveKnownMerchant(String normalizedMerchant, String expectedDisplayName) {
        var result = merchantResolver.resolve(normalizedMerchant);

        assertThat(result.matched()).isTrue();
        assertThat(result.exactMatch()).isTrue();
        assertThat(result.confidence()).isEqualTo(1.0);
        assertThat(result.displayName()).isEqualTo(expectedDisplayName);
        assertThat(result.merchantId()).isNotBlank();
    }

    @Test
    void shouldNotMatchUnknownMerchant() {
        var result = merchantResolver.resolve("UNKNOWN SHOP");

        assertThat(result.matched()).isFalse();
        assertThat(result.exactMatch()).isFalse();
        assertThat(result.confidence()).isZero();
        assertThat(result.merchantId()).isNull();
        assertThat(result.displayName()).isNull();
    }

    @Test
    void shouldNotMatchEmptyValue() {
        var result = merchantResolver.resolve("   ");

        assertThat(result.matched()).isFalse();
        assertThat(result.confidence()).isZero();
    }

    @Test
    void shouldNotMatchNullValue() {
        var result = merchantResolver.resolve(null);

        assertThat(result.matched()).isFalse();
        assertThat(result.confidence()).isZero();
    }
}
