ALTER TABLE produto
    ADD COLUMN estoque_minimo INT NOT NULL DEFAULT 0,
    ADD COLUMN prazo_reposicao_dias INT NOT NULL DEFAULT 0;

ALTER TABLE inventory_history
    ADD COLUMN created_by INT,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by INT,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE inventory_alert (
    id INT AUTO_INCREMENT PRIMARY KEY,
    organization_id INT NOT NULL,
    produto_id INT NOT NULL,
    severity VARCHAR(16) NOT NULL,
    dias_restantes INT,
    consumo_medio_diario DECIMAL(12, 2) NOT NULL,
    estoque_atual INT NOT NULL,
    estoque_minimo INT NOT NULL,
    prazo_reposicao_dias INT NOT NULL,
    quantidade_sugerida INT NOT NULL,
    data_estimada_ruptura DATE,
    mensagem VARCHAR(255),
    created_by INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_alert_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
);
