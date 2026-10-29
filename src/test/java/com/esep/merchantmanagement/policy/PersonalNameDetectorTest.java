package com.esep.merchantmanagement.policy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalNameDetectorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "ИП Тестов Т.",
            "ИП Тестова А.Б.",
            "ИП ТЕСТОВ Т",
            "IP TESTOV T.",
            "Тестов Т.",
            "ИП Тестов",
            "ИП Тестова Анна",
            "ИП Тестенко",
            "ИП Тест Тестович",
            "ИП Тестов Тестұлы",
            "ИП Тестова Тест Тестовна",
            "ИП"
    })
    void shouldTreatPersonNamesAsPersonal(String name) {
        assertThat(PersonalNameDetector.looksLikePersonalName(name)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ИП Gamer",
            "IP GAMER CLUB",
            "ИП «Ромашка»",
            "ИП Ромашка",
            "ИП Pizza House",
            "ИП Shop 24",
            "MAGNUM CASH&CARRY",
            "ТОО Тестовый магазин",
            "TEST STORE",
            "Технодом"
    })
    void shouldKeepTradeNames(String name) {
        assertThat(PersonalNameDetector.looksLikePersonalName(name)).isFalse();
    }
}
