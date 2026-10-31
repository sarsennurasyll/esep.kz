package com.esep.entity;

import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Личное правило назначения операций по контрагенту. Не является частью общей базы продавцов.
 */
@Entity
@Table(
        name = "counterparty_purpose_rules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_counterparty_purpose_rules_key_direction",
                columnNames = {"counterparty_key", "direction"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CounterpartyPurposeRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Владелец строки; задаётся и проверяется Hibernate, см. ADR-0009. */
    @TenantId
    @Column(name = "owner_id", updatable = false)
    private Long ownerId;

    @NotBlank
    @Size(max = 1_000)
    @Column(nullable = false, length = 1_000)
    private String counterpartyKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MoneyDirection direction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MoneyPurpose purpose;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
