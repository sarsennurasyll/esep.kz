package com.esep.moneyflow.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Агрегат операций одного типа движения и назначения: суммы поступлений и списаний (обе неотрицательные).
 */
public record MoneyFlowTotal(
        MoneyFlowType flowType,
        MoneyPurpose purpose,
        BigDecimal inflow,
        BigDecimal outflow,
        long operationCount
) {

    public MoneyFlowTotal {
        Objects.requireNonNull(flowType, "Flow type must not be null");
        Objects.requireNonNull(purpose, "Purpose must not be null");
        inflow = inflow == null ? BigDecimal.ZERO : inflow;
        outflow = outflow == null ? BigDecimal.ZERO : outflow;
        if (inflow.signum() < 0 || outflow.signum() < 0) {
            throw new IllegalArgumentException("Inflow and outflow must not be negative");
        }
    }
}
