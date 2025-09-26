ALTER TABLE cliente
    ADD COLUMN lifetime_value DECIMAL(12, 2) NOT NULL DEFAULT 0;

UPDATE cliente
SET lifetime_value = 0
WHERE lifetime_value IS NULL;

ALTER TABLE cliente
    ALTER COLUMN lifetime_value DROP DEFAULT;
