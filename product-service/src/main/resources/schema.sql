CREATE TABLE IF NOT EXISTS products (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(150)   NOT NULL,
    description  TEXT,
    price        DECIMAL(10,2)  NOT NULL,
    category     VARCHAR(100)   NOT NULL,
    stock_hint   INT            DEFAULT 0,   -- display only, real stock in inventory-service
    active       BOOLEAN        DEFAULT TRUE,
    created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );