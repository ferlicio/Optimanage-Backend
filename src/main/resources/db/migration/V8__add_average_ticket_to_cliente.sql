ALTER TABLE cliente
    ADD COLUMN average_ticket DECIMAL(12, 2) NOT NULL DEFAULT 0;

UPDATE cliente
SET average_ticket = 0
WHERE average_ticket IS NULL;

ALTER TABLE cliente
    ALTER COLUMN average_ticket DROP DEFAULT;
