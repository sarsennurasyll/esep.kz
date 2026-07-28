package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.api.dto.MerchantMatchRequest;
import com.esep.merchantmanagement.api.dto.MerchantResponse;
import com.esep.merchantmanagement.api.dto.UnknownMerchantResponse;

import java.util.List;

/**
 * Клиент HTTP API управления продавцами для web-интерфейса.
 */
public interface MerchantWebApiClient {

    List<UnknownMerchantResponse> findUnknownDescriptions();

    List<MerchantResponse> findMerchants();

    void match(MerchantMatchRequest request);
}
