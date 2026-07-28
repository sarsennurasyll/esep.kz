package com.esep.persistence.jpa.read.repository;

import java.math.BigDecimal;

/**
 * Проекция расходов по категории.
 */
public interface CategoryExpenseProjection {

    String getCategory();

    BigDecimal getAmount();
}
