package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.api.dto.MerchantMatchRequest;
import com.esep.merchantmanagement.api.dto.MerchantResponse;
import com.esep.merchantmanagement.api.dto.UnknownMerchantResponse;
import com.esep.merchantmanagement.api.dto.MerchantLearningStatisticsResponse;

import java.util.List;

/**
 * Клиент HTTP API управления продавцами для web-интерфейса.
 */
public interface MerchantWebApiClient {

    List<UnknownMerchantResponse> findUnknownDescriptions(String query, boolean onlyNew, Long minUsageCount, java.math.BigDecimal minTotalAmount);

    default List<UnknownMerchantResponse> findUnknownDescriptions() {
        return findUnknownDescriptions(null, false, null, null);
    }

    List<MerchantResponse> findMerchants();

    void match(MerchantMatchRequest request);

    MerchantLearningStatisticsResponse learningStatistics();
}
