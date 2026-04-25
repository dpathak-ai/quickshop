CREATE TABLE IF NOT EXISTS inventory (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id   BIGINT         NOT NULL UNIQUE,
    product_name VARCHAR(150)   NOT NULL,
    quantity     INT            NOT NULL DEFAULT 0,
    reserved     INT            NOT NULL DEFAULT 0,   -- currently reserved, not yet confirmed
    version      BIGINT         NOT NULL DEFAULT 0,   -- optimistic locking
    updated_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );