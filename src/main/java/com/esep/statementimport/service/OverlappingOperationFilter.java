package com.esep.statementimport.service;

import com.esep.entity.BankOperationType;
import com.esep.persistence.model.ExistingOperationKey;
import com.esep.statementimport.model.ParsedTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Отбрасывает операции новой выписки, которые уже сохранены из прежних выписок того же счёта (ADR-0004).
 * В выписке нет идентификатора операции, а одинаковые строки законно повторяются, поэтому сравнивается
 * количество одинаковых операций: из новой выписки добавляется только превышение над уже сохранённым.
 */
class OverlappingOperationFilter {

    OverlapResult filter(List<ParsedTransaction> transactions, List<ExistingOperationKey> existingOperations) {
        return filter(transactions, existingOperations, null, null);
    }

    /**
     * @param periodFrom первый день новой выписки; операции этого дня не считаются пропавшими
     * @param periodTo последний день новой выписки; выписка за период, заканчивающийся сегодня, может не содержать поздних операций дня
     */
    OverlapResult filter(
            List<ParsedTransaction> transactions,
            List<ExistingOperationKey> existingOperations,
            LocalDate periodFrom,
            LocalDate periodTo
    ) {
        Map<Key, Integer> remainingExisting = new HashMap<>();
        for (ExistingOperationKey existing : existingOperations) {
            remainingExisting.merge(
                    Key.of(existing.date(), existing.amount(), existing.bankOperationType(), existing.description()),
                    1,
                    Integer::sum
            );
        }

        List<ParsedTransaction> newTransactions = new ArrayList<>();
        int skipped = 0;
        for (ParsedTransaction transaction : transactions) {
            Key key = Key.of(transaction.date(), transaction.amount(), transaction.bankOperationType(), transaction.description());
            Integer remaining = remainingExisting.get(key);
            if (remaining != null && remaining > 0) {
                remainingExisting.put(key, remaining - 1);
                skipped++;
            } else {
                newTransactions.add(transaction);
            }
        }
        // Остаток — операции, которые есть в прежних выписках за общий период, но отсутствуют в новой.
        // Их не удаляют: пользователь видит число и сам решает, какой выписке верить (ADR-0004).
        int missing = remainingExisting.entrySet().stream()
                .filter(entry -> !entry.getKey().date().equals(periodFrom) && !entry.getKey().date().equals(periodTo))
                .mapToInt(Map.Entry::getValue)
                .sum();
        return new OverlapResult(newTransactions, skipped, missing);
    }

    /**
     * @param missingFromNewStatement операции прежних выписок за общий период, которых нет в новой, без граничных дней
     */
    record OverlapResult(List<ParsedTransaction> newTransactions, int skippedDuplicates, int missingFromNewStatement) {

        OverlapResult {
            newTransactions = List.copyOf(newTransactions);
        }
    }

    private record Key(LocalDate date, String amount, BankOperationType bankOperationType, String description) {

        static Key of(LocalDate date, BigDecimal amount, BankOperationType bankOperationType, String description) {
            // Сумма сравнивается по значению (100 и 100.00 — одна сумма), описание — без лишних пробелов.
            return new Key(
                    date,
                    amount.stripTrailingZeros().toPlainString(),
                    bankOperationType,
                    description == null ? "" : description.strip().replaceAll("\\s+", " ")
            );
        }
    }
}
