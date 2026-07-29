INSERT INTO merchants (original_name, normalized_name, category_id, merchant_type, confidence, detection_source, verified, times_matched, confirmed_count, rejected_count, created_at, updated_at)
VALUES ('Переводы людям', 'ПЕРЕВОДЫ ЛЮДЯМ', (SELECT id FROM categories WHERE code = 'PERSONAL_TRANSFERS'), 'PERSON', 1.00, 'DATABASE', TRUE, 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (normalized_name) DO NOTHING;
