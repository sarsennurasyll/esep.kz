WITH normalized_transactions AS (
    SELECT id,
           RTRIM(
                   UPPER(
                           REGEXP_REPLACE(
                                   BTRIM(
                                           REGEXP_REPLACE(
                                                   description,
                                                   '^(ИП|ТОО|TOO|LLP)[[:space:]]+',
                                                   '',
                                                   'i'
                                           )
                                   ),
                                   '[[:space:]]+',
                                   ' ',
                                   'g'
                           )
                   ),
                   '. '
           ) AS normalized_description
    FROM transactions
    WHERE merchant_id IS NULL
), person_merchants AS (
    SELECT id, normalized_name
    FROM merchants
    WHERE merchant_type = 'PERSON'
)
UPDATE transactions transaction_row
SET merchant_id = person_merchant.id
FROM normalized_transactions normalized_transaction
JOIN person_merchants person_merchant
    ON person_merchant.normalized_name = normalized_transaction.normalized_description
WHERE transaction_row.id = normalized_transaction.id;
