CREATE TABLE IF NOT EXISTS orders (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT          NOT NULL,
    product_id    BIGINT          NOT NULL,
    product_name  VARCHAR(150)    NOT NULL,
    quantity      INT             NOT NULL,
    unit_price    DECIMAL(10,2)   NOT NULL,
    total_price   DECIMAL(10,2)   NOT NULL,
    status        ENUM('PENDING','CONFIRMED','FAILED') DEFAULT 'PENDING',
    failure_reason VARCHAR(255),
    trace_id      VARCHAR(64),
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS outbox_events (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id  VARCHAR(64)     NOT NULL,    -- orderId
    event_type    VARCHAR(100)    NOT NULL,    -- "order.placed"
    payload       JSON            NOT NULL,    -- full event as JSON
    published     BOOLEAN         DEFAULT FALSE,
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    published_at  TIMESTAMP       NULL,

    INDEX idx_unpublished (published, created_at)  -- poller query performance
    );