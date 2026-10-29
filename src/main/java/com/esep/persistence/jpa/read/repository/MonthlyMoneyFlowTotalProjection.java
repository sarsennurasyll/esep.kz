package com.esep.persistence.jpa.read.repository;

/** Проекция агрегата движения денег за месяц. */
public interface MonthlyMoneyFlowTotalProjection extends MoneyFlowTotalProjection {

    int getYear();

    int getMonth();
}
