ALTER TABLE transactions
    ADD COLUMN bank_operation_type VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN';
