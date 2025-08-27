-- Initial Flyway migration
CREATE TABLE fornecedor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(64) NOT NULL
);

