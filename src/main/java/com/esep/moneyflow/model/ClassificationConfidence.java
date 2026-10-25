package com.esep.moneyflow.model;

/**
 * Уровень уверенности решения. Правила не дают настоящей вероятности, поэтому используется шкала уровней,
 * а не дробное число с ложной точностью.
 */
public enum ClassificationConfidence {
    HIGH,
    MEDIUM,
    LOW,
    NONE
}
