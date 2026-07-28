package com.esep.persistence.jpa.read.adapter;

import com.esep.merchantmanagement.interfaces.MerchantLearningStatisticsQuery;
import com.esep.merchantmanagement.model.MerchantLearningStatistics;
import com.esep.persistence.jpa.read.repository.MerchantLearningStatisticsReadJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantLearningStatisticsJpaAdapter implements MerchantLearningStatisticsQuery {
    private final MerchantLearningStatisticsReadJpaRepository repository;
    private final UnknownMerchantDescriptionJpaAdapter unknownDescriptions;

    public MerchantLearningStatisticsJpaAdapter(MerchantLearningStatisticsReadJpaRepository repository,
                                                UnknownMerchantDescriptionJpaAdapter unknownDescriptions) {
        this.repository = repository;
        this.unknownDescriptions = unknownDescriptions;
    }

    @Override
    public MerchantLearningStatistics getStatistics() {
        var statistics = repository.getStatistics();
        if (statistics == null) {
            return new MerchantLearningStatistics(0, 0, 0, 0, 0);
        }
        return new MerchantLearningStatistics(statistics.getMerchantCount(), statistics.getAliasCount(),
                unknownDescriptions.findAll().size(), statistics.getRecognizedTransactionCount(), statistics.getUnknownTransactionCount());
    }
}
