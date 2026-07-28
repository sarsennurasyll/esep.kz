package com.esep.persistence.jpa.read.repository;

/**
 * Проекция количества операций по категории.
 */
public interface CategoryOperationCountProjection {

    String getCategory();

    String getCategoryName();

    long getTransactionCount();
}
