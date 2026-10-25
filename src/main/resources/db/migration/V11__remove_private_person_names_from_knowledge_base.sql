-- Общая база знаний хранит только организации. Имена частных лиц из ранних seed-миграций и ручных
-- подтверждений удаляются; операции сохраняют связь с системным получателем «Переводы людям».
UPDATE transactions transaction_row
SET merchant_id = system_merchant.id
FROM merchants person
JOIN merchants system_merchant ON system_merchant.normalized_name = 'ПЕРЕВОДЫ ЛЮДЯМ'
WHERE transaction_row.merchant_id = person.id
  AND person.merchant_type = 'PERSON'
  AND person.id <> system_merchant.id;

DELETE FROM merchant_aliases alias
USING merchants merchant
WHERE alias.merchant_id = merchant.id
  AND merchant.merchant_type = 'PERSON';

DELETE FROM merchants person
USING merchants system_merchant
WHERE system_merchant.normalized_name = 'ПЕРЕВОДЫ ЛЮДЯМ'
  AND person.merchant_type = 'PERSON'
  AND person.id <> system_merchant.id;
