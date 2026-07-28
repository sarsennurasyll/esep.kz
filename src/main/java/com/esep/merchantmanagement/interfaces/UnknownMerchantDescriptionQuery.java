package com.esep.merchantmanagement.interfaces;

import com.esep.merchantmanagement.model.UnknownMerchantCandidate;

import java.util.List;

/**
 * Read-порт агрегированных описаний операций без Merchant.
 */
public interface UnknownMerchantDescriptionQuery {

    List<UnknownMerchantCandidate> findAll();
}
