INSERT INTO categories (code, name, icon, color, active)
VALUES
    ('RESTAURANTS', 'Кафе и рестораны', 'cup-hot', '#fd7e14', TRUE),
    ('CAR', 'Автомобиль', 'car-front', '#6c757d', TRUE),
    ('HEALTH', 'Здоровье', 'heart-pulse', '#dc3545', TRUE),
    ('ENTERTAINMENT', 'Развлечения', 'ticket-perforated', '#d63384', TRUE),
    ('SUBSCRIPTIONS', 'Подписки', 'repeat', '#6f42c1', TRUE),
    ('HOUSING', 'Жильё', 'house-door', '#0d6efd', TRUE),
    ('BANKING', 'Банковские услуги', 'bank', '#198754', TRUE),
    ('GOVERNMENT', 'Государственные услуги', 'building', '#795548', TRUE),
    ('EDUCATION', 'Образование', 'mortarboard', '#0dcaf0', TRUE),
    ('WORK', 'Работа', 'briefcase', '#6610f2', TRUE),
    ('GIFTS', 'Подарки', 'gift', '#e83e8c', TRUE)
ON CONFLICT (code) DO NOTHING;
