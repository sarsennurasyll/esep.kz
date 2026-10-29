package com.esep.onetimecheck.model;

import java.math.BigDecimal;

/** Покупки одной категории за вычетом возвратов. */
public record CategorySpending(String category, BigDecimal amount) {
}
