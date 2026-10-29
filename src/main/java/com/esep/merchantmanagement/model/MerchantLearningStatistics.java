package com.esep.merchantmanagement.model;

/**
 * Сводная статистика развития базы знаний продавцов.
 * Покрытие считается только по покупкам и возвратам покупок: у переводов, пополнений и снятий продавца нет.
 * Это доля покупок, у которых найден продавец, а не точность: верность найденного продавца здесь не проверяется.
 */
public record MerchantLearningStatistics(
        long merchantCount, long aliasCount, long unknownDescriptionCount,
        long purchasesWithMerchantCount, long purchasesWithoutMerchantCount
) {
    public double merchantCoveragePercent() {
        long total = purchasesWithMerchantCount + purchasesWithoutMerchantCount;
        return total == 0 ? 0 : purchasesWithMerchantCount * 100.0 / total;
    }
}
