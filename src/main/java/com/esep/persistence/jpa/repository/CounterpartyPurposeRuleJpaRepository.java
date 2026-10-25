package com.esep.persistence.jpa.repository;

import com.esep.entity.CounterpartyPurposeRuleEntity;
import com.esep.moneyflow.model.MoneyDirection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounterpartyPurposeRuleJpaRepository extends JpaRepository<CounterpartyPurposeRuleEntity, Long> {

    Optional<CounterpartyPurposeRuleEntity> findByCounterpartyKeyAndDirection(String counterpartyKey, MoneyDirection direction);
}
