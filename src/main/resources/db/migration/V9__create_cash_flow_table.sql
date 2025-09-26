CREATE TABLE cash_flow_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    organization_id INT NOT NULL,
    description VARCHAR(128) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    movement_date DATE NOT NULL,
    cancelled_at DATETIME NULL,
    created_by INT,
    created_at DATETIME NOT NULL,
    updated_by INT,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_cash_flow_entries_org_date ON cash_flow_entries (organization_id, movement_date);
