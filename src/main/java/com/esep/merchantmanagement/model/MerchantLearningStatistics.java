package com.esep.merchantmanagement.model;

/** Сводная статистика развития базы знаний продавцов. */
public record MerchantLearningStatistics(
        long merchantCount, long aliasCount, long unknownDescriptionCount,
        long recognizedTransactionCount, long unknownTransactionCount
) {
    public double recognitionPercent() {
        long total = recognizedTransactionCount + unknownTransactionCount;
        return total == 0 ? 0 : recognizedTransactionCount * 100.0 / total;
    }
}
