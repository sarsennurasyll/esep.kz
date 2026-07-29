INSERT INTO merchants (original_name, normalized_name, category_id, merchant_type, confidence, detection_source, verified, times_matched, confirmed_count, rejected_count, created_at, updated_at)
SELECT 'Переводы людям', 'ПЕРЕВОДЫ ЛЮДЯМ', category.id, 'PERSON', 1.00, 'DATABASE', TRUE, 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM categories category WHERE category.code = 'PERSONAL_TRANSFERS'
ON CONFLICT (normalized_name) DO NOTHING;

INSERT INTO merchant_aliases (alias_name, normalized_alias, merchant_id, verified, created_at)
SELECT person.original_name, person.normalized_name, system_merchant.id, TRUE, CURRENT_TIMESTAMP
FROM merchants person
JOIN merchants system_merchant ON system_merchant.normalized_name = 'ПЕРЕВОДЫ ЛЮДЯМ'
WHERE person.merchant_type = 'PERSON' AND person.id <> system_merchant.id
ON CONFLICT (normalized_alias) DO NOTHING;

UPDATE transactions transaction_row SET merchant_id = system_merchant.id
FROM merchants person JOIN merchants system_merchant ON system_merchant.normalized_name = 'ПЕРЕВОДЫ ЛЮДЯМ'
WHERE transaction_row.merchant_id = person.id AND person.merchant_type = 'PERSON' AND person.id <> system_merchant.id;

UPDATE merchant_aliases alias SET merchant_id = system_merchant.id
FROM merchants person JOIN merchants system_merchant ON system_merchant.normalized_name = 'ПЕРЕВОДЫ ЛЮДЯМ'
WHERE alias.merchant_id = person.id AND person.merchant_type = 'PERSON' AND person.id <> system_merchant.id;

DELETE FROM merchants person USING merchants system_merchant
WHERE person.merchant_type = 'PERSON' AND person.id <> system_merchant.id
  AND NOT EXISTS (SELECT 1 FROM transactions transaction_row WHERE transaction_row.merchant_id = person.id)
  AND NOT EXISTS (SELECT 1 FROM merchant_aliases alias WHERE alias.merchant_id = person.id);
