package com.esep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Загруженная банковская выписка за указанный период.
 */
@Entity
@Table(
        name = "statements",
        indexes = @Index(name = "idx_statements_bank_period", columnList = "bank_name, period_from, period_to")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BankType bankName;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Size(max = 34)
    @Column(length = 34)
    private String accountNumber;

    @NotBlank
    @Size(min = 64, max = 64)
    @Column(nullable = false, unique = true, length = 64, updatable = false)
    private String sourceFileHash;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    @NotNull
    @Column(nullable = false)
    private LocalDate periodFrom;

    @NotNull
    @Column(nullable = false)
    private LocalDate periodTo;
}
