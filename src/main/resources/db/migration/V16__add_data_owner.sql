-- Владелец личных данных (ADR-0009). Старые строки остаются без владельца и никому не видны,
-- пока их явно не передадут учётной записи настройкой ESEP_LEGACY_DATA_OWNER_EMAIL.
ALTER TABLE statements ADD COLUMN owner_id BIGINT;
ALTER TABLE statements ADD CONSTRAINT fk_statements_owner FOREIGN KEY (owner_id) REFERENCES user_accounts (id);
-- Хеш уникален только у одного владельца: иначе ответ 409 раскрывал бы, что файл уже загружал кто-то другой.
ALTER TABLE statements DROP CONSTRAINT uk_statements_source_file_hash;
ALTER TABLE statements ADD CONSTRAINT uk_statements_owner_source_file_hash UNIQUE (owner_id, source_file_hash);
ALTER TABLE statements ADD CONSTRAINT uk_statements_id_owner UNIQUE (id, owner_id);

ALTER TABLE transactions ADD COLUMN owner_id BIGINT;
-- Операция не может принадлежать не тому, кому принадлежит её выписка.
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_statement_owner
    FOREIGN KEY (statement_id, owner_id) REFERENCES statements (id, owner_id);
CREATE INDEX idx_transactions_owner_id ON transactions (owner_id);

ALTER TABLE counterparty_purpose_rules ADD COLUMN owner_id BIGINT;
ALTER TABLE counterparty_purpose_rules ADD CONSTRAINT fk_counterparty_purpose_rules_owner
    FOREIGN KEY (owner_id) REFERENCES user_accounts (id);
ALTER TABLE counterparty_purpose_rules DROP CONSTRAINT uk_counterparty_purpose_rules_key_direction;
ALTER TABLE counterparty_purpose_rules ADD CONSTRAINT uk_counterparty_purpose_rules_owner_key_direction
    UNIQUE (owner_id, counterparty_key, direction);
