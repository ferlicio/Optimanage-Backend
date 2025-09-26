ALTER TABLE cliente
    ADD COLUMN churn_score DECIMAL(5, 4) NOT NULL DEFAULT 0;

UPDATE cliente
SET churn_score = 0
WHERE churn_score IS NULL;

ALTER TABLE cliente
    ALTER COLUMN churn_score DROP DEFAULT;
