package com.esep.moneyflow.interfaces;

import com.esep.moneyflow.model.CounterpartyActivity;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;

import java.util.Collection;
import java.util.List;

/** Операции, назначение которых размечает пользователь. */
public interface PurposeLabelingTransactions {

    List<CounterpartyActivity> findActivity(Collection<MoneyFlowType> flowTypes);

    /** Устанавливает назначение по правилу; решения, принятые пользователем для отдельной операции, не меняются. */
    long applyRulePurpose(Collection<String> descriptions, MoneyDirection direction, Collection<MoneyFlowType> flowTypes, MoneyPurpose purpose);

    /** Возвращает в «неизвестно» только назначения, установленные правилом. */
    long resetRulePurpose(Collection<String> descriptions, MoneyDirection direction, Collection<MoneyFlowType> flowTypes);
}
