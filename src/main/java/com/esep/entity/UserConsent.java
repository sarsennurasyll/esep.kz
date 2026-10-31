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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Запись журнала: какую версию документа и когда принял или отозвал пользователь. Строки не изменяются.
 */
@Entity
@Table(name = "user_consents", indexes = @Index(name = "idx_user_consents_account_id", columnList = "account_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, updatable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    private LegalDocument document;

    @Column(nullable = false, updatable = false, length = 20)
    private String version;

    @Column(nullable = false, updatable = false)
    private boolean granted;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant recordedAt;
}
