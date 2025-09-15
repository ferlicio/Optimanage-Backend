CREATE TABLE user_invite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    organization_id INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(32) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    email VARCHAR(255),
    created_by_id INT NOT NULL,
    used_by_id INT,
    CONSTRAINT fk_invite_created_by FOREIGN KEY (created_by_id) REFERENCES user(id),
    CONSTRAINT fk_invite_used_by FOREIGN KEY (used_by_id) REFERENCES user(id),
    CONSTRAINT fk_invite_org FOREIGN KEY (organization_id) REFERENCES organization(id)
);
