package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;

import java.util.Map;
import java.util.Optional;

/**
 * Соответствие названий типов операций Kaspi Gold документальным типам операций.
 * В строке операции тип указан в единственном числе, в итогах шапки — во множественном.
 */
final class KaspiOperationTypes {

    private static final Map<String, BankOperationType> OPERATION_NAMES = Map.of(
            "Покупка", BankOperationType.PURCHASE,
            "Перевод", BankOperationType.TRANSFER,
            "Пополнение", BankOperationType.TOP_UP,
            "Снятие", BankOperationType.CASH_WITHDRAWAL,
            "Разное", BankOperationType.OTHER
    );

    private static final Map<String, BankOperationType> SUMMARY_NAMES = Map.of(
            "Покупки", BankOperationType.PURCHASE,
            "Переводы", BankOperationType.TRANSFER,
            "Пополнения", BankOperationType.TOP_UP,
            "Снятия", BankOperationType.CASH_WITHDRAWAL,
            "Разное", BankOperationType.OTHER
    );

    private KaspiOperationTypes() {
    }

    static Optional<BankOperationType> fromOperationName(String name) {
        return Optional.ofNullable(OPERATION_NAMES.get(name));
    }

    static Optional<BankOperationType> fromSummaryName(String name) {
        return Optional.ofNullable(SUMMARY_NAMES.get(name));
    }

    static String summaryNamesPattern() {
        return String.join("|", SUMMARY_NAMES.keySet());
    }
}
