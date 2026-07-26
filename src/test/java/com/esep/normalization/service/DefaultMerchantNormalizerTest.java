package com.esep.normalization.service;

import com.esep.normalization.model.NormalizedMerchant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMerchantNormalizerTest {

    private final DefaultMerchantNormalizer normalizer = new DefaultMerchantNormalizer();

    @Test
    void shouldReturnEmptyResultForEmptyString() {
        NormalizedMerchant result = normalizer.normalize("   ");

        assertThat(result.normalizedName()).isEmpty();
        assertThat(result.confidence()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnEmptyResultForNull() {
        NormalizedMerchant result = normalizer.normalize(null);

        assertThat(result.originalName()).isNull();
        assertThat(result.normalizedName()).isEmpty();
        assertThat(result.confidence()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldTrimExtraWhitespace() {
        NormalizedMerchant result = normalizer.normalize("   MAGNUM    ");

        assertThat(result.normalizedName()).isEqualTo("MAGNUM");
        assertThat(result.confidence()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void shouldConvertLowerCaseToUpperCase() {
        NormalizedMerchant result = normalizer.normalize("magnum");

        assertThat(result.normalizedName()).isEqualTo("MAGNUM");
    }

    @Test
    void shouldRemoveBoundarySpecialCharacters() {
        NormalizedMerchant result = normalizer.normalize("***MAGNUM***");

        assertThat(result.normalizedName()).isEqualTo("MAGNUM");
    }

    @Test
    void shouldNormalizeRepeatedSpaces() {
        NormalizedMerchant result = normalizer.normalize("MAGNUM      EXPRESS");

        assertThat(result.normalizedName()).isEqualTo("MAGNUM EXPRESS");
    }
}
