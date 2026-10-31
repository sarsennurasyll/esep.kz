package com.esep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Название для пополнения общей базы, присланное по согласию. Намеренно не связано с человеком (ADR-0010).
 */
@Entity
@Table(name = "merchant_suggestions")
@Getter
@NoArgsConstructor
public class MerchantSuggestion {

    public static final String STATUS_NEW = "NEW";

    @Id
    @Column(name = "normalized_name", length = 1000)
    private String normalizedName;

    @Column(nullable = false, length = 10)
    private String status;
}
