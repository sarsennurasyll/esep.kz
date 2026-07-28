package com.esep.merchantmanagement.interfaces;

import com.esep.merchantresolver.model.MerchantReference;

import java.util.Collection;

/**
 * Привязывает ранее импортированные неизвестные операции к продавцу.
 */
public interface MerchantTransactionBindingCatalog {

    long bindUnknownTransactions(Collection<String> descriptions, MerchantReference merchantReference);
}
