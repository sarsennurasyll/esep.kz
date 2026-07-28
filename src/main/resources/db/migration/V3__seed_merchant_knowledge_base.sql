INSERT INTO categories (code, name, icon, color, active)
VALUES
    ('GROCERY', 'Продукты', 'basket', '#198754', TRUE),
    ('PHARMACY', 'Аптеки', 'heart-pulse', '#dc3545', TRUE),
    ('TAXI', 'Такси', 'taxi-front', '#ffc107', TRUE),
    ('PUBLIC_TRANSPORT', 'Общественный транспорт', 'bus-front', '#0d6efd', TRUE),
    ('MOBILE_COMMUNICATION', 'Мобильная связь', 'phone', '#6f42c1', TRUE),
    ('GAMES', 'Игры', 'controller', '#fd7e14', TRUE),
    ('ELECTRONICS', 'Электроника', 'laptop', '#0dcaf0', TRUE),
    ('DIGITAL_SERVICES', 'Цифровые сервисы', 'cloud', '#20c997', TRUE),
    ('SHOPPING', 'Покупки', 'bag', '#d63384', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO merchants (
    original_name,
    normalized_name,
    category_id,
    confidence,
    detection_source,
    verified,
    times_matched,
    confirmed_count,
    rejected_count,
    created_at,
    updated_at
)
SELECT merchant.original_name,
       merchant.normalized_name,
       category.id,
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
        ('MAGNUM', 'MAGNUM', 'GROCERY'),
        ('EUROPHARMA', 'EUROPHARMA', 'PHARMACY'),
        ('YANDEX GO', 'YANDEX GO', 'TAXI'),
        ('ONAY', 'ONAY', 'PUBLIC_TRANSPORT'),
        ('AVTOBYS', 'AVTOBYS', 'PUBLIC_TRANSPORT'),
        ('ACTIV', 'ACTIV', 'MOBILE_COMMUNICATION'),
        ('ALTEL', 'ALTEL', 'MOBILE_COMMUNICATION'),
        ('TELE2', 'TELE2', 'MOBILE_COMMUNICATION'),
        ('KCELL', 'KCELL', 'MOBILE_COMMUNICATION'),
        ('SUPERCELL', 'SUPERCELL', 'GAMES'),
        ('STEAM', 'STEAM', 'GAMES'),
        ('GOOGLE PLAY', 'GOOGLE PLAY', 'DIGITAL_SERVICES'),
        ('APPLE', 'APPLE', 'DIGITAL_SERVICES'),
        ('SMALL', 'SMALL', 'GROCERY'),
        ('TECHNODOM', 'TECHNODOM', 'ELECTRONICS'),
        ('WILDBERRIES', 'WILDBERRIES', 'SHOPPING'),
        ('OZON', 'OZON', 'SHOPPING'),
        ('FLO', 'FLO', 'SHOPPING')
) AS merchant(original_name, normalized_name, category_code)
JOIN categories category ON category.code = merchant.category_code
ON CONFLICT (normalized_name) DO NOTHING;

INSERT INTO merchant_aliases (alias_name, normalized_alias, merchant_id, created_at, verified)
SELECT alias.alias_name,
       alias.normalized_alias,
       merchant.id,
       CURRENT_TIMESTAMP,
       TRUE
FROM (
    VALUES
        ('MAGNUM CASH&CARRY', 'MAGNUM CASH&CARRY', 'MAGNUM'),
        ('YANDEX.GO', 'YANDEX.GO', 'YANDEX GO'),
        ('ONAY. Пополнение баланса', 'ONAY. ПОПОЛНЕНИЕ БАЛАНСА', 'ONAY'),
        ('Билет Onay. Оплата проезда', 'БИЛЕТ ONAY. ОПЛАТА ПРОЕЗДА', 'ONAY'),
        ('ONAY.KZ', 'ONAY.KZ', 'ONAY'),
        ('Билет Avtobys. Оплата проезда', 'БИЛЕТ AVTOBYS. ОПЛАТА ПРОЕЗДА', 'AVTOBYS'),
        ('FS *SUPERCELLSTORE', 'FS *SUPERCELLSTORE', 'SUPERCELL'),
        ('STEAM PURCHASE', 'STEAM PURCHASE', 'STEAM')
) AS alias(alias_name, normalized_alias, merchant_normalized_name)
JOIN merchants merchant ON merchant.normalized_name = alias.merchant_normalized_name
ON CONFLICT (normalized_alias) DO NOTHING;
