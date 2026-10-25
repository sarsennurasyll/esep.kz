-- Модель движения денег (ADR-0003): тип движения и назначение хранятся отдельно от банковского типа операции.
ALTER TABLE transactions
    ADD COLUMN flow_type VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN flow_source VARCHAR(20) NOT NULL DEFAULT 'NONE',
    ADD COLUMN flow_confidence VARCHAR(10) NOT NULL DEFAULT 'NONE',
    ADD COLUMN purpose VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN purpose_source VARCHAR(20) NOT NULL DEFAULT 'NONE';

-- Однократное заполнение уже сохранённых операций теми же правилами, что BaseMoneyFlowRules и KaspiMoneyFlowClassifier.
UPDATE transactions transaction_row
SET flow_type = CASE
        WHEN bank_operation_type = 'PURCHASE' AND amount < 0 THEN 'PURCHASE'
        WHEN bank_operation_type = 'PURCHASE' AND amount > 0 THEN 'PURCHASE_REFUND'
        WHEN bank_operation_type = 'CASH_WITHDRAWAL' AND amount < 0 THEN 'CASH_WITHDRAWAL'
        WHEN bank_operation_type = 'TRANSFER' AND amount < 0 THEN 'TRANSFER_TO_PERSON'
        WHEN bank_operation_type = 'TRANSFER' AND amount > 0 THEN 'TRANSFER_FROM_PERSON'
        WHEN bank_operation_type = 'OTHER' AND amount < 0 THEN 'BANK_FEE'
        WHEN statement.bank_name = 'KASPI' AND bank_operation_type = 'TOP_UP' AND amount > 0
             AND BTRIM(description) IN ('В Kaspi Банкомате', 'В Kaspi Терминале') THEN 'CASH_DEPOSIT'
        WHEN statement.bank_name = 'KASPI' AND bank_operation_type = 'TOP_UP' AND amount > 0
             AND BTRIM(description) ~ '^[[:upper:]][[:alpha:]]+( [[:upper:]][[:alpha:]]+)? [[:upper:]]\.$' THEN 'TRANSFER_FROM_PERSON'
        ELSE 'UNKNOWN'
    END
FROM statements statement
WHERE statement.id = transaction_row.statement_id;

UPDATE transactions
SET flow_source = 'BANK_RULE',
    flow_confidence = CASE
        WHEN flow_type IN ('PURCHASE', 'PURCHASE_REFUND', 'CASH_WITHDRAWAL', 'CASH_DEPOSIT') THEN 'HIGH'
        ELSE 'MEDIUM'
    END
WHERE flow_type <> 'UNKNOWN';

CREATE INDEX idx_transactions_flow_type ON transactions (flow_type);
