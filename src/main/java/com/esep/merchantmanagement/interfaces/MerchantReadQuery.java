package com.esep.merchantmanagement.interfaces;

import com.esep.merchantmanagement.model.MerchantSummary;

import java.util.List;

/**
 * Read-порт списка известных продавцов.
 */
public interface MerchantReadQuery {

    List<MerchantSummary> findAll();
}
