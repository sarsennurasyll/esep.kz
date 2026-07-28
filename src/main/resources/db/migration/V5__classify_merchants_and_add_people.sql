ALTER TABLE merchants
    ADD COLUMN merchant_type VARCHAR(30) NOT NULL DEFAULT 'OTHER';

INSERT INTO categories (code, name, icon, color, active)
VALUES ('PERSONAL_TRANSFERS', 'Переводы людям', 'person-arrows-right-left', '#6f42c1', TRUE)
ON CONFLICT (code) DO NOTHING;

UPDATE merchants
SET merchant_type = CASE normalized_name
    WHEN 'MAGNUM' THEN 'STORE'
    WHEN 'SMALL' THEN 'STORE'
    WHEN 'FLO' THEN 'STORE'
    WHEN 'EUROPHARMA' THEN 'PHARMACY'
    WHEN 'YANDEX GO' THEN 'TAXI'
    WHEN 'ONAY' THEN 'PUBLIC_TRANSPORT'
    WHEN 'AVTOBYS' THEN 'PUBLIC_TRANSPORT'
    WHEN 'ACTIV' THEN 'MOBILE_OPERATOR'
    WHEN 'ALTEL' THEN 'MOBILE_OPERATOR'
    WHEN 'TELE2' THEN 'MOBILE_OPERATOR'
    WHEN 'KCELL' THEN 'MOBILE_OPERATOR'
    WHEN 'SUPERCELL' THEN 'DIGITAL_SERVICE'
    WHEN 'STEAM' THEN 'DIGITAL_SERVICE'
    WHEN 'GOOGLE PLAY' THEN 'DIGITAL_SERVICE'
    WHEN 'APPLE' THEN 'DIGITAL_SERVICE'
    WHEN 'TECHNODOM' THEN 'STORE'
    WHEN 'WILDBERRIES' THEN 'MARKETPLACE'
    WHEN 'OZON' THEN 'MARKETPLACE'
    ELSE merchant_type
END;

INSERT INTO merchants (
    original_name,
    normalized_name,
    category_id,
    merchant_type,
    confidence,
    detection_source,
    verified,
    times_matched,
    confirmed_count,
    rejected_count,
    created_at,
    updated_at
)
SELECT person.original_name,
       person.normalized_name,
       category.id,
       'PERSON',
       1.00,
       'DATABASE',
       TRUE,
       0,
       0,
       0,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM (
    VALUES
        ('ТОЛЕШБАЕВА', 'ТОЛЕШБАЕВА'),
        ('ЕРАСЫЛ Е', 'ЕРАСЫЛ Е'),
        ('ЗАИР З', 'ЗАИР З'),
        ('САЯН С', 'САЯН С')
) AS person(original_name, normalized_name)
JOIN categories category ON category.code = 'PERSONAL_TRANSFERS'
ON CONFLICT (normalized_name) DO UPDATE
SET category_id = EXCLUDED.category_id,
    merchant_type = EXCLUDED.merchant_type,
    confidence = EXCLUDED.confidence,
    detection_source = EXCLUDED.detection_source,
    verified = EXCLUDED.verified,
    updated_at = CURRENT_TIMESTAMP;
