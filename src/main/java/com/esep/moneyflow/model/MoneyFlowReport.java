package com.esep.moneyflow.model;

import java.math.BigDecimal;

/**
 * Отчёт о движении денег, в котором каждая сумма учтена ровно в одной строке.
 *
 * @param income              поступления, которые пользователь отметил как доход
 * @param expense             покупки за вычетом возвратов, комиссии и списания с назначением «расход»
 * @param cashWithdrawals     снятие наличных: назначение неизвестно, но это не перевод и не покупка
 * @param familyInflow        поступления от семьи и подарки
 * @param familyOutflow       переводы семье и подарки
 * @param internalInflow      свои деньги: внесение наличных, переводы со своих счетов, возвраты долгов
 * @param internalOutflow     переводы на свои счета и выданные долги
 * @param unknownInflow       поступления с неизвестным назначением
 * @param unknownOutflow      списания с неизвестным назначением
 * @param totalInflow         все поступления по выпискам
 * @param totalOutflow        все списания по выпискам
 * @param unknownInflowShare  доля поступлений с неизвестным назначением, от 0 до 1
 * @param unknownOutflowShare доля списаний с неизвестным назначением, включая наличные, от 0 до 1
 */
public record MoneyFlowReport(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal cashWithdrawals,
        BigDecimal familyInflow,
        BigDecimal familyOutflow,
        BigDecimal internalInflow,
        BigDecimal internalOutflow,
        BigDecimal unknownInflow,
        BigDecimal unknownOutflow,
        BigDecimal totalInflow,
        BigDecimal totalOutflow,
        BigDecimal unknownInflowShare,
        BigDecimal unknownOutflowShare
) {
}
