package com.esep.persistence.jpa.read.repository;

import com.esep.entity.MerchantType;
import com.esep.entity.Transaction;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Read-репозиторий SQL-агрегатов аналитики операций.
 */
public interface AnalyticsTransactionReadJpaRepository extends Repository<Transaction, Long> {

    @Query("""
            select count(transaction) as totalTransactions,
                   coalesce(sum(case when transaction.amount > 0 then transaction.amount else 0 end), 0) as totalIncome,
                   coalesce(sum(case when transaction.amount < 0 then -transaction.amount else 0 end), 0) as totalExpense,
                   coalesce(sum(case when transaction.flowType in (com.esep.moneyflow.model.MoneyFlowType.PURCHASE, com.esep.moneyflow.model.MoneyFlowType.PURCHASE_REFUND)
                                 and transaction.merchant is not null then 1 else 0 end), 0) as purchasesWithMerchant,
                   coalesce(sum(case when transaction.flowType in (com.esep.moneyflow.model.MoneyFlowType.PURCHASE, com.esep.moneyflow.model.MoneyFlowType.PURCHASE_REFUND)
                                 and transaction.merchant is null then 1 else 0 end), 0) as purchasesWithoutMerchant
            from Transaction transaction
            """)
    AnalyticsSummaryProjection getSummary();

    // Траты по категориям — только покупки за вычетом возвратов, как строка «Расходы» в отчёте о движении денег.
    // Переводы людям и наличные сюда не входят: у них нет категории покупки.
    @Query("""
            select coalesce(category.code, 'UNCATEGORIZED') as category,
                   coalesce(category.name, 'Без категории') as categoryName,
                   coalesce(sum(-transaction.amount), 0) as amount
            from Transaction transaction
            left join transaction.merchant merchant
            left join merchant.category category
            where ((transaction.flowType = com.esep.moneyflow.model.MoneyFlowType.PURCHASE and transaction.amount < 0)
                   or (transaction.flowType = com.esep.moneyflow.model.MoneyFlowType.PURCHASE_REFUND and transaction.amount > 0))
            group by coalesce(category.code, 'UNCATEGORIZED'), coalesce(category.name, 'Без категории')
            having sum(-transaction.amount) > 0
            order by sum(-transaction.amount) desc
            """)
    List<CategoryExpenseProjection> findCategoryExpenses();

    @Query("""
            select coalesce(category.code, 'UNCATEGORIZED') as category,
                   coalesce(category.name, 'Без категории') as categoryName,
                   count(transaction) as transactionCount
            from Transaction transaction
            left join transaction.merchant merchant
            left join merchant.category category
            where transaction.flowType = com.esep.moneyflow.model.MoneyFlowType.PURCHASE and transaction.amount < 0
            group by coalesce(category.code, 'UNCATEGORIZED'), coalesce(category.name, 'Без категории')
            order by count(transaction) desc, coalesce(category.name, 'Без категории') asc
            """)
    List<CategoryOperationCountProjection> findCategoryOperationCounts();

    @Query("""
            select merchant.originalName as merchant,
                   coalesce(sum(-transaction.amount), 0) as amount,
                   count(transaction) as transactionCount
            from Transaction transaction
            join transaction.merchant merchant
            where transaction.amount < 0
            group by merchant.originalName
            order by sum(-transaction.amount) desc, merchant.originalName asc
            """)
    List<MerchantExpenseProjection> findTopMerchantExpenses(Pageable pageable);

    @Query("""
            select merchant.merchantType as merchantType,
                   coalesce(sum(-transaction.amount), 0) as amount,
                   count(transaction) as transactionCount
            from Transaction transaction
            join transaction.merchant merchant
            where transaction.amount < 0
            group by merchant.merchantType
            order by sum(-transaction.amount) desc, merchant.merchantType asc
            """)
    List<MerchantTypeExpenseProjection> findMerchantTypeExpenses();

    @Query("""
            select merchant.originalName as recipient,
                   coalesce(sum(-transaction.amount), 0) as amount,
                   count(transaction) as transactionCount
            from Transaction transaction
            join transaction.merchant merchant
            where transaction.amount < 0
              and merchant.merchantType = :merchantType
            group by merchant.originalName
            order by sum(-transaction.amount) desc, merchant.originalName asc
            """)
    List<PersonTransferProjection> findTopPersonTransfers(
            @Param("merchantType") MerchantType merchantType,
            Pageable pageable
    );

    @Query("""
            select year(transaction.transactionDate) as year,
                   month(transaction.transactionDate) as month,
                   coalesce(sum(case when transaction.amount > 0 then transaction.amount else 0 end), 0) as income,
                   coalesce(sum(case when transaction.amount < 0 then -transaction.amount else 0 end), 0) as expense
            from Transaction transaction
            group by year(transaction.transactionDate), month(transaction.transactionDate)
            order by year(transaction.transactionDate) asc, month(transaction.transactionDate) asc
            """)
    List<MonthlyAnalyticsProjection> findMonthlyAnalytics();

    @Query("""
            select transaction.flowType as flowType,
                   transaction.purpose as purpose,
                   coalesce(sum(case when transaction.amount > 0 then transaction.amount else 0 end), 0) as inflow,
                   coalesce(sum(case when transaction.amount < 0 then -transaction.amount else 0 end), 0) as outflow,
                   count(transaction) as operationCount
            from Transaction transaction
            group by transaction.flowType, transaction.purpose
            """)
    List<MoneyFlowTotalProjection> findMoneyFlowTotals();

    @Query("""
            select year(transaction.transactionDate) as year,
                   month(transaction.transactionDate) as month,
                   transaction.flowType as flowType,
                   transaction.purpose as purpose,
                   coalesce(sum(case when transaction.amount > 0 then transaction.amount else 0 end), 0) as inflow,
                   coalesce(sum(case when transaction.amount < 0 then -transaction.amount else 0 end), 0) as outflow,
                   count(transaction) as operationCount
            from Transaction transaction
            group by year(transaction.transactionDate), month(transaction.transactionDate), transaction.flowType, transaction.purpose
            order by year(transaction.transactionDate), month(transaction.transactionDate)
            """)
    List<MonthlyMoneyFlowTotalProjection> findMonthlyMoneyFlowTotals();
}
