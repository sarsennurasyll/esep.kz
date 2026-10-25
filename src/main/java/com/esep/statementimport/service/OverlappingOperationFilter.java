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
        return new OverlapResult(newTransactions, skipped);
    }

    record OverlapResult(List<ParsedTransaction> newTransactions, int skippedDuplicates) {

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
