package com.esep.moneyflow.service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Ключ контрагента для личных правил: описание операции без различий регистра, пробелов и
 * совместимых форм Unicode. Ключ не утверждает, что разные описания — один и тот же человек.
 */
public final class CounterpartyKeys {

    private CounterpartyKeys() {
    }

    public static String of(String description) {
        if (description == null) {
            return "";
        }
        String normalized = Normalizer.normalize(description, Normalizer.Form.NFKC)
                .strip()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
        // В выписках Kaspi казахская «ә» иногда записана латинской «ə»; приводим к кириллице.
        return normalized.replace('Ə', 'Ә');
    }
}
