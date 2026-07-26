package com.esep.normalization.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizationRulesTest {

    @Test
    void shouldTrimValue() {
        assertThat(new TrimRule().apply("  MAGNUM  ")).isEqualTo("MAGNUM");
    }

    @Test
    void shouldConvertValueToUpperCase() {
        assertThat(new UpperCaseRule().apply("magnum")).isEqualTo("MAGNUM");
    }

    @Test
    void shouldNormalizeRepeatedSpaces() {
        assertThat(new NormalizeSpacesRule().apply("MAGNUM   EXPRESS")).isEqualTo("MAGNUM EXPRESS");
    }

    @Test
    void shouldRemoveBoundarySpecialCharacters() {
        assertThat(new RemoveSpecialCharactersRule().apply("***MAGNUM***")).isEqualTo("MAGNUM");
    }

    @Test
    void shouldRemoveLegalPrefix() {
        assertThat(new RemoveLegalPrefixRule().apply("ТОО MAGNUM")).isEqualTo("MAGNUM");
    }

    @Test
    void shouldRemoveLocationSuffix() {
        assertThat(new RemoveLocationSuffixRule().apply("MAGNUM ALMATY")).isEqualTo("MAGNUM");
    }

    @Test
    void shouldRemoveBranchNumber() {
        assertThat(new RemoveBranchNumberRule().apply("MAGNUM #145")).isEqualTo("MAGNUM");
    }
}
