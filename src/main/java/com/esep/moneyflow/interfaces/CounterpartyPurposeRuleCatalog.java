package com.esep.moneyflow.interfaces;

import com.esep.moneyflow.model.CounterpartyPurposeRule;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;

import java.util.List;
import java.util.Optional;

/** Хранилище личных правил назначения по контрагенту. */
public interface CounterpartyPurposeRuleCatalog {

    List<CounterpartyPurposeRule> findAll();

    Optional<CounterpartyPurposeRule> find(String counterpartyKey, MoneyDirection direction);

    /** Создаёт правило или меняет назначение существующего правила для контрагента и направления. */
    CounterpartyPurposeRule save(String counterpartyKey, MoneyDirection direction, MoneyPurpose purpose);

    Optional<CounterpartyPurposeRule> delete(long ruleId);
}
