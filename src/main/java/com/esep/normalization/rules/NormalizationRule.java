package com.esep.normalization.rules;

/**
 * Контракт одного преобразования названия продавца.
 */
public interface NormalizationRule {

    String apply(String value);
}
