package com.esep.merchantmanagement.interfaces;

import com.esep.merchantmanagement.model.MerchantSummary;
import com.esep.merchantmanagement.model.UnknownMerchantDescription;
import com.esep.merchantresolver.model.MerchantReference;

import java.util.List;

/**
 * Прикладной сценарий просмотра и подтверждения неизвестных продавцов.
 */
public interface MerchantManagementService {

    List<UnknownMerchantDescription> findUnknownDescriptions();

    List<MerchantSummary> findMerchants();

    void match(String normalizedDescription, MerchantReference merchantReference);
}
