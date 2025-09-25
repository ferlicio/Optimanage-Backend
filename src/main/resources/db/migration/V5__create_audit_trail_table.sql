CREATE TABLE audit_trail (
    id INT AUTO_INCREMENT PRIMARY KEY,
    organization_id INT NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id INT NOT NULL,
    action VARCHAR(150) NOT NULL,
    details TEXT,
    created_by INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_trail_org_entity ON audit_trail (organization_id, entity_type);
CREATE INDEX idx_audit_trail_entity_id ON audit_trail (entity_id);
