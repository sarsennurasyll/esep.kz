package com.esep.merchantresolver.model;

import java.util.Set;

/**
 * Неизменяемая запись известного продавца и его вариантов названий.
 */
public record MerchantRecord(
        String id,
        String canonicalName,
        Set<String> aliases
) {

    public MerchantRecord {
        aliases = Set.copyOf(aliases);
    }
}
