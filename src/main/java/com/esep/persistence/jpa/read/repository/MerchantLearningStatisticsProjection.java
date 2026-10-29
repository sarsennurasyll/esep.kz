package com.esep.persistence.jpa.read.repository;

public interface MerchantLearningStatisticsProjection {
    long getMerchantCount();
    long getAliasCount();
    long getPurchasesWithMerchantCount();
    long getPurchasesWithoutMerchantCount();
}
