package com.esep.merchantmanagement.model;

import com.esep.merchantresolver.model.MerchantReference;

/**
 * Команда на создание подтверждённого соответствия описания и продавца.
 */
public record MerchantAliasMatchCommand(
        String aliasName,
        String normalizedAlias,
        MerchantReference merchantReference
) {
}
