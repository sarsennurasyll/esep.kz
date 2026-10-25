package com.esep.moneyflow.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyDirectionTest {

    @Test
    void shouldAllowIncomeOnlyForInflowAndExpenseOnlyForOutflow() {
        assertThat(MoneyDirection.INFLOW.allows(MoneyPurpose.INCOME_WORK)).isTrue();
        assertThat(MoneyDirection.OUTFLOW.allows(MoneyPurpose.INCOME_WORK)).isFalse();
        assertThat(MoneyDirection.OUTFLOW.allows(MoneyPurpose.EXPENSE)).isTrue();
        assertThat(MoneyDirection.INFLOW.allows(MoneyPurpose.EXPENSE)).isFalse();
        assertThat(MoneyDirection.INFLOW.allows(MoneyPurpose.DEBT)).isTrue();
        assertThat(MoneyDirection.OUTFLOW.allows(MoneyPurpose.FAMILY)).isTrue();
    }

    @Test
    void shouldDetectDirectionBySign() {
        assertThat(MoneyDirection.of(new BigDecimal("1"))).isEqualTo(MoneyDirection.INFLOW);
        assertThat(MoneyDirection.of(new BigDecimal("-1"))).isEqualTo(MoneyDirection.OUTFLOW);
    }
}
