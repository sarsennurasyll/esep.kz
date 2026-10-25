package com.esep.persistence.jpa.read.adapter;

import com.esep.moneyflow.interfaces.PurposeLabelingTransactions;
import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.CounterpartyActivity;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.persistence.jpa.read.repository.CounterpartyActivityProjection;
import com.esep.persistence.jpa.read.repository.PurposeLabelingJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * JPA-адаптер операций, назначение которых размечает пользователь.
 */
@Repository
public class PurposeLabelingTransactionsJpaAdapter implements PurposeLabelingTransactions {

    private final PurposeLabelingJpaRepository repository;

    public PurposeLabelingTransactionsJpaAdapter(PurposeLabelingJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CounterpartyActivity> findActivity(Collection<MoneyFlowType> flowTypes) {
        return Stream.concat(
                repository.findInflowActivity(flowTypes).stream().map(row -> toActivity(row, MoneyDirection.INFLOW)),
                repository.findOutflowActivity(flowTypes).stream().map(row -> toActivity(row, MoneyDirection.OUTFLOW))
        ).toList();
    }

    @Override
    public long applyRulePurpose(Collection<String> descriptions, MoneyDirection direction, Collection<MoneyFlowType> flowTypes, MoneyPurpose purpose) {
        if (descriptions.isEmpty()) {
            return 0;
        }
        return repository.updatePurpose(descriptions, flowTypes, direction == MoneyDirection.INFLOW, purpose, ClassificationSource.PERSONAL_RULE);
    }

    @Override
    public long resetRulePurpose(Collection<String> descriptions, MoneyDirection direction, Collection<MoneyFlowType> flowTypes) {
        if (descriptions.isEmpty()) {
            return 0;
        }
        return repository.resetRulePurpose(descriptions, flowTypes, direction == MoneyDirection.INFLOW);
    }

    private CounterpartyActivity toActivity(CounterpartyActivityProjection row, MoneyDirection direction) {
        return new CounterpartyActivity(
                row.getDescription(),
                direction,
                row.getPurpose(),
                row.getAmount() == null ? BigDecimal.ZERO : row.getAmount(),
                row.getOperationCount()
        );
    }
}
