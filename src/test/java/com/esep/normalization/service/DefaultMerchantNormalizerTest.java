package com.esep.normalization.service;

import com.esep.normalization.model.NormalizedMerchant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest
    @CsvSource({
            "'ИП АБДУРАХМАНОВ', АБДУРАХМАНОВ",
            "'ТОО MAGNUM', MAGNUM",
            "'TOO MAGNUM', MAGNUM",
            "'LLP MAGNUM', MAGNUM"
    })
    void shouldRemoveLegalEntityPrefix(String source, String expected) {
        NormalizedMerchant result = normalizer.normalize(source);

        assertThat(result.normalizedName()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "'MAGNUM #145', MAGNUM",
            "'MAGNUM №12', MAGNUM"
    })
    void shouldRemoveBranchNumber(String source, String expected) {
        NormalizedMerchant result = normalizer.normalize(source);

        assertThat(result.normalizedName()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "'MAGNUM KZ', MAGNUM",
            "'MAGNUM KAZAKHSTAN', MAGNUM",
            "'MAGNUM ALMATY', MAGNUM",
            "'MAGNUM ASTANA', MAGNUM"
    })
    void shouldRemoveLocationSuffix(String source, String expected) {
        NormalizedMerchant result = normalizer.normalize(source);

        assertThat(result.normalizedName()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "'MAGNUM EXPRESS', 'MAGNUM EXPRESS'",
            "'MAGNUM EXPERT', 'MAGNUM EXPERT'",
            "'MAGNUM MARKET', 'MAGNUM MARKET'",
            "'MAGNUM STORE', 'MAGNUM STORE'"
    })
    void shouldPreserveOrdinaryWords(String source, String expected) {
        NormalizedMerchant result = normalizer.normalize(source);

        assertThat(result.normalizedName()).isEqualTo(expected);
    }

    @Test
    void shouldApplySeveralRulesInOrder() {
        NormalizedMerchant result = normalizer.normalize("  TOO MAGNUM #145 ALMATY  ");

        assertThat(result.normalizedName()).isEqualTo("MAGNUM");
    }
}
