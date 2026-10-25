package com.esep.moneyflow.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CounterpartyKeysTest {

    @Test
    void shouldIgnoreCaseAndWhitespace() {
        assertThat(CounterpartyKeys.of("  Тестов   Т. ")).isEqualTo(CounterpartyKeys.of("ТЕСТОВ Т."));
    }

    @Test
    void shouldTreatLatinSchwaAsKazakhLetter() {
        assertThat(CounterpartyKeys.of("Тəстов Т.")).isEqualTo(CounterpartyKeys.of("Тәстов Т."));
    }

    @Test
    void shouldReturnEmptyKeyForMissingDescription() {
        assertThat(CounterpartyKeys.of(null)).isEmpty();
    }
}
