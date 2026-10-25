package com.esep.persistence.jpa.adapter;

import com.esep.entity.CounterpartyPurposeRuleEntity;
import com.esep.moneyflow.interfaces.CounterpartyPurposeRuleCatalog;
import com.esep.moneyflow.model.CounterpartyPurposeRule;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.persistence.jpa.repository.CounterpartyPurposeRuleJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-адаптер личных правил назначения по контрагенту.
 */
@Repository
public class CounterpartyPurposeRuleCatalogJpaAdapter implements CounterpartyPurposeRuleCatalog {

    private final CounterpartyPurposeRuleJpaRepository repository;

    public CounterpartyPurposeRuleCatalogJpaAdapter(CounterpartyPurposeRuleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CounterpartyPurposeRule> findAll() {
        return repository.findAll().stream().map(this::toModel).toList();
    }

    @Override
    public Optional<CounterpartyPurposeRule> find(String counterpartyKey, MoneyDirection direction) {
        return repository.findByCounterpartyKeyAndDirection(counterpartyKey, direction).map(this::toModel);
    }

    @Override
    public CounterpartyPurposeRule save(String counterpartyKey, MoneyDirection direction, MoneyPurpose purpose) {
        CounterpartyPurposeRuleEntity rule = repository.findByCounterpartyKeyAndDirection(counterpartyKey, direction)
                .orElseGet(() -> CounterpartyPurposeRuleEntity.builder().counterpartyKey(counterpartyKey).direction(direction).build());
        rule.setPurpose(purpose);
        return toModel(repository.saveAndFlush(rule));
    }

    @Override
    public Optional<CounterpartyPurposeRule> delete(long ruleId) {
        Optional<CounterpartyPurposeRuleEntity> rule = repository.findById(ruleId);
        rule.ifPresent(repository::delete);
        return rule.map(this::toModel);
    }

    private CounterpartyPurposeRule toModel(CounterpartyPurposeRuleEntity entity) {
        return new CounterpartyPurposeRule(entity.getId(), entity.getCounterpartyKey(), entity.getDirection(), entity.getPurpose());
    }
}
