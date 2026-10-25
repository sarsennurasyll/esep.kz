package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;
import com.esep.entity.BankType;
import com.esep.moneyflow.interfaces.MoneyFlowClassifier;
import com.esep.moneyflow.model.ClassificationConfidence;
import com.esep.moneyflow.model.MoneyFlowClassification;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.service.BaseMoneyFlowRules;

import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Правила Kaspi Gold для пополнений: в выписке «Пополнение» означает и внесение наличных,
 * и перевод от человека, и поступление с карты другого банка, которая может принадлежать самому владельцу.
 */
public class KaspiMoneyFlowClassifier implements MoneyFlowClassifier {

    private static final Set<String> CASH_DEPOSIT_DETAILS = Set.of("В Kaspi Банкомате", "В Kaspi Терминале");
    // «Имя Ф.» или «Имя Фамилия Ф.» в кириллице или латинице, включая казахские буквы.
    private static final Pattern PERSON_NAME = Pattern.compile("^\\p{Lu}\\p{L}+(?: \\p{Lu}\\p{L}+)? \\p{Lu}\\.$");

    @Override
    public BankType supportedBank() {
        return BankType.KASPI;
    }

    @Override
    public MoneyFlowClassification classify(BankOperationType bankOperationType, BigDecimal amount, String description) {
        if (bankOperationType != BankOperationType.TOP_UP || amount.signum() <= 0) {
            return BaseMoneyFlowRules.classify(bankOperationType, amount);
        }

        String details = description == null ? "" : description.strip();
        if (CASH_DEPOSIT_DETAILS.contains(details)) {
            return MoneyFlowClassification.byBankRule(MoneyFlowType.CASH_DEPOSIT, ClassificationConfidence.HIGH);
        }
        // Имя с указанием другого банка, карта другого банка и кошельки могут быть собственными деньгами владельца:
        // это знает только пользователь.
        if (PERSON_NAME.matcher(details).matches()) {
            return MoneyFlowClassification.byBankRule(MoneyFlowType.TRANSFER_FROM_PERSON, ClassificationConfidence.MEDIUM);
        }
        return MoneyFlowClassification.unknown();
    }
}
