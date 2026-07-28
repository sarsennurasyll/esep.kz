package com.esep.merchantmanagement.api.dto;

import com.esep.merchantmanagement.model.MerchantLearningStatistics;

public record MerchantLearningStatisticsResponse(long merchantCount, long aliasCount, long unknownDescriptionCount,
                                                  long recognizedTransactionCount, long unknownTransactionCount,
                                                  double recognitionPercent) {
    public static MerchantLearningStatisticsResponse from(MerchantLearningStatistics statistics) {
        return new MerchantLearningStatisticsResponse(statistics.merchantCount(), statistics.aliasCount(), statistics.unknownDescriptionCount(),
                statistics.recognizedTransactionCount(), statistics.unknownTransactionCount(), statistics.recognitionPercent());
    }
}
