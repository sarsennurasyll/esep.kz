package com.esep.statementimport.kaspi;

import com.esep.entity.BankOperationType;
import com.esep.moneyflow.model.ClassificationConfidence;
import com.esep.moneyflow.model.ClassificationSource;
import com.esep.moneyflow.model.MoneyFlowClassification;
import com.esep.moneyflow.model.MoneyFlowType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class KaspiMoneyFlowClassifierTest {

    private final KaspiMoneyFlowClassifier classifier = new KaspiMoneyFlowClassifier();

    @Test
    void shouldRecognizeCashDepositThroughKaspiDevices() {
        assertThat(classify(BankOperationType.TOP_UP, "10000", "В Kaspi Банкомате").flowType()).isEqualTo(MoneyFlowType.CASH_DEPOSIT);
        assertThat(classify(BankOperationType.TOP_UP, "10000", "В Kaspi Терминале").confidence()).isEqualTo(ClassificationConfidence.HIGH);
    }

    @Test
    void shouldTreatNamedTopUpAsTransferFromPersonWithMediumConfidence() {
        MoneyFlowClassification cyrillic = classify(BankOperationType.TOP_UP, "5000", "Тестов Т.");
        MoneyFlowClassification kazakhLetters = classify(BankOperationType.TOP_UP, "5000", "Әсемтест Қ.");
        MoneyFlowClassification latin = classify(BankOperationType.TOP_UP, "5000", "Testov T.");

        assertThat(cyrillic.flowType()).isEqualTo(MoneyFlowType.TRANSFER_FROM_PERSON);
        assertThat(cyrillic.confidence()).isEqualTo(ClassificationConfidence.MEDIUM);
        assertThat(kazakhLetters.flowType()).isEqualTo(MoneyFlowType.TRANSFER_FROM_PERSON);
        assertThat(latin.flowType()).isEqualTo(MoneyFlowType.TRANSFER_FROM_PERSON);
    }

    @Test
    void shouldLeaveSourcesThatMayBeOwnMoneyUnknown() {
        assertThat(classify(BankOperationType.TOP_UP, "5000", "С карты другого банка").flowType()).isEqualTo(MoneyFlowType.UNKNOWN);
        assertThat(classify(BankOperationType.TOP_UP, "5000", "Тестов Т., Halyk Bank").flowType()).isEqualTo(MoneyFlowType.UNKNOWN);
        assertThat(classify(BankOperationType.TOP_UP, "5000", "TESTPAY*Wallet").source()).isEqualTo(ClassificationSource.NONE);
    }

    @Test
    void shouldApplyBaseRulesToOtherOperationTypes() {
        assertThat(classify(BankOperationType.PURCHASE, "-1000", "TEST STORE").flowType()).isEqualTo(MoneyFlowType.PURCHASE);
        assertThat(classify(BankOperationType.PURCHASE, "1000", "TEST STORE").flowType()).isEqualTo(MoneyFlowType.PURCHASE_REFUND);
        assertThat(classify(BankOperationType.TRANSFER, "-1000", "Тестов Т.").flowType()).isEqualTo(MoneyFlowType.TRANSFER_TO_PERSON);
        assertThat(classify(BankOperationType.CASH_WITHDRAWAL, "-9000", "Банкомат TEST").flowType()).isEqualTo(MoneyFlowType.CASH_WITHDRAWAL);
        assertThat(classify(BankOperationType.OTHER, "-500", "Плата за обслуживание").flowType()).isEqualTo(MoneyFlowType.BANK_FEE);
        assertThat(classify(BankOperationType.UNKNOWN, "300", "Бонусы Начисление").flowType()).isEqualTo(MoneyFlowType.UNKNOWN);
    }

    private MoneyFlowClassification classify(BankOperationType type, String amount, String description) {
        return classifier.classify(type, new BigDecimal(amount), description);
    }
}
