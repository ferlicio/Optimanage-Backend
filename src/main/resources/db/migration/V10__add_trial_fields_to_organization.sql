ALTER TABLE organization
    ADD COLUMN trial_inicio DATE NULL,
    ADD COLUMN trial_fim DATE NULL,
    ADD COLUMN trial_tipo VARCHAR(32) NULL;
