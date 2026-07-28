WITH normalized_transactions AS (
    SELECT id,
           UPPER(REGEXP_REPLACE(BTRIM(description), '\s+', ' ', 'g')) AS normalized_description
    FROM transactions
    WHERE merchant_id IS NULL
), resolved_merchants AS (
    SELECT transaction_row.id AS transaction_id,
           COALESCE(alias.merchant_id, merchant.id) AS merchant_id
    FROM normalized_transactions transaction_row
    LEFT JOIN merchant_aliases alias
        ON alias.normalized_alias = transaction_row.normalized_description
    LEFT JOIN merchants merchant
        ON merchant.normalized_name = transaction_row.normalized_description
)
UPDATE transactions transaction_row
SET merchant_id = resolved_merchants.merchant_id
FROM resolved_merchants
WHERE transaction_row.id = resolved_merchants.transaction_id
  AND resolved_merchants.merchant_id IS NOT NULL;
