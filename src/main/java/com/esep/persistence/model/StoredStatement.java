package com.esep.persistence.model;

import com.esep.entity.BankType;

import java.time.LocalDate;

/**
 * Сохранённая выписка: признаки, по которым выписки одного счёта сравнивались при импорте.
 *
 * @param accountNumber маска карты {@code ****NNNN} или {@code null}, если счёт не определён
 */
public record StoredStatement(
        long id,
        BankType bank,
        String accountNumber,
        LocalDate periodFrom,
        LocalDate periodTo
) {
}
