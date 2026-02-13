-- Table creation script for Payment Example V2
-- This script creates table templates that can be used by Hibernate's hbm2ddl.auto=create
-- For production, use Hibernate's auto DDL or Flyway/Liquibase migrations

-- Note: In this example, we rely on Hibernate's hbm2ddl.auto=create to create tables
-- The actual sharded tables will be created automatically by Hibernate based on ShardingSphere configuration
-- This script is provided for reference and manual table creation if needed

-- Orders table template (will be sharded as t_orders_0 to t_orders_15)

CREATE TABLE IF NOT EXISTS t_orders_template
(
    uuid              VARCHAR(64) PRIMARY KEY,
    version_number    BIGINT                      DEFAULT 1,
    created_date      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modified_date     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted           VARCHAR(64),
    locked            BOOLEAN                     DEFAULT FALSE,
    disabled          BOOLEAN                     DEFAULT FALSE,

    order_id          VARCHAR(64)    NOT NULL UNIQUE,
    user_id           BIGINT         NOT NULL,
    shop_id           BIGINT,
    shop_name         VARCHAR(128),

    total_amount      NUMERIC(18, 2) NOT NULL,
    discount_amount   NUMERIC(18, 2)              DEFAULT 0.00,
    shipping_fee      NUMERIC(18, 2)              DEFAULT 0.00,
    actual_amount     NUMERIC(18, 2) NOT NULL,
    currency          VARCHAR(8)                  DEFAULT 'CNY',

    order_status      SMALLINT       NOT NULL     DEFAULT 0,
    pay_status        SMALLINT                    DEFAULT 0,
    shipping_status   SMALLINT                    DEFAULT 0,

    receiver_name     VARCHAR(64)    NOT NULL,
    receiver_phone    VARCHAR(32)    NOT NULL,
    receiver_address  TEXT           NOT NULL,
    receiver_postcode VARCHAR(16),

    pay_time          TIMESTAMP,
    ship_time         TIMESTAMP,
    complete_time     TIMESTAMP,
    cancel_time       TIMESTAMP,
    expire_time       TIMESTAMP,

    user_remark       VARCHAR(512),
    admin_remark      VARCHAR(512),

    source            SMALLINT                    DEFAULT 0,
    channel           VARCHAR(32),
    promotion_info    TEXT
);

CREATE INDEX idx_orders_order_id ON t_orders_template (order_id);
CREATE INDEX idx_orders_user_id ON t_orders_template (user_id);
CREATE INDEX idx_orders_shop_id ON t_orders_template (shop_id);
CREATE INDEX idx_orders_order_status ON t_orders_template (order_status);
CREATE INDEX idx_orders_pay_status ON t_orders_template (pay_status);
CREATE INDEX idx_orders_created_date ON t_orders_template (created_date);
CREATE INDEX idx_orders_pay_time ON t_orders_template (pay_time);
CREATE INDEX idx_orders_deleted ON t_orders_template (deleted);
CREATE INDEX idx_orders_user_status ON t_orders_template (user_id, order_status);
CREATE INDEX idx_orders_shop_status ON t_orders_template (shop_id, order_status);

CREATE TABLE IF NOT EXISTS t_order_items_template
(
    uuid             VARCHAR(64) PRIMARY KEY,
    version_number   BIGINT                  DEFAULT 1,
    created_date     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    modified_date    TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    deleted          VARCHAR(64),

    order_id         VARCHAR(64)    NOT NULL,
    user_id          BIGINT         NOT NULL,
    product_id       BIGINT         NOT NULL,
    sku_id           BIGINT,

    product_name     VARCHAR(256)   NOT NULL,
    product_image    VARCHAR(512),
    sku_spec         VARCHAR(256),

    unit_price       NUMERIC(18, 2) NOT NULL,
    quantity         INT            NOT NULL DEFAULT 1,
    total_price      NUMERIC(18, 2) NOT NULL,
    discount_amount  NUMERIC(18, 2)          DEFAULT 0.00,

    product_category VARCHAR(64),
    brand_name       VARCHAR(64)
);

CREATE INDEX idx_order_items_order_id ON t_order_items_template (order_id);
CREATE INDEX idx_order_items_user_id ON t_order_items_template (user_id);
CREATE INDEX idx_order_items_product_id ON t_order_items_template (product_id);
CREATE INDEX idx_order_items_sku_id ON t_order_items_template (sku_id);
CREATE INDEX idx_order_items_deleted ON t_order_items_template (deleted);


CREATE TABLE IF NOT EXISTS t_payments_template
(
    uuid                 VARCHAR(64) PRIMARY KEY,
    version_number       BIGINT                  DEFAULT 1,
    created_date         TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    modified_date        TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    deleted              VARCHAR(64),

    payment_id           VARCHAR(64)    NOT NULL UNIQUE,
    order_id             VARCHAR(64)    NOT NULL,
    user_id              BIGINT         NOT NULL,

    payment_amount       NUMERIC(18, 2) NOT NULL,
    currency             VARCHAR(8)              DEFAULT 'CNY',

    payment_method       SMALLINT       NOT NULL,
    payment_channel      VARCHAR(32),
    payment_status       SMALLINT       NOT NULL DEFAULT 0,

    third_party_trade_no VARCHAR(128),
    third_party_order_no VARCHAR(128),

    pay_time             TIMESTAMP,
    expire_time          TIMESTAMP,
    notify_time          TIMESTAMP,

    notify_url           VARCHAR(512),
    return_url           VARCHAR(512),
    callback_data        TEXT,

    error_code           VARCHAR(32),
    error_message        VARCHAR(256),

    client_ip            VARCHAR(64),
    device_info          VARCHAR(128),
    remark               VARCHAR(512)
);

CREATE INDEX idx_payments_payment_id ON t_payments_template (payment_id);
CREATE INDEX idx_payments_order_id ON t_payments_template (order_id);
CREATE INDEX idx_payments_user_id ON t_payments_template (user_id);
CREATE INDEX idx_payments_payment_status ON t_payments_template (payment_status);
CREATE INDEX idx_payments_third_party_trade_no ON t_payments_template (third_party_trade_no);
CREATE INDEX idx_payments_created_date ON t_payments_template (created_date);
CREATE INDEX idx_payments_pay_time ON t_payments_template (pay_time);
CREATE INDEX idx_payments_deleted ON t_payments_template (deleted);
CREATE INDEX idx_payments_user_status ON t_payments_template (user_id, payment_status);


CREATE TABLE IF NOT EXISTS t_payment_records_template
(
    uuid                 VARCHAR(64) PRIMARY KEY,
    version_number       BIGINT    DEFAULT 1,
    created_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted              VARCHAR(64),

    payment_id           VARCHAR(64) NOT NULL,
    order_id             VARCHAR(64) NOT NULL,
    user_id              BIGINT      NOT NULL,

    record_type          SMALLINT    NOT NULL,
    old_status           SMALLINT,
    new_status           SMALLINT,

    operator_type        SMALLINT,
    operator_id          VARCHAR(64),
    operator_name        VARCHAR(64),

    amount               NUMERIC(18, 2),
    third_party_trade_no VARCHAR(128),
    remark               VARCHAR(512),
    extra_data           TEXT
);

CREATE INDEX idx_payment_records_payment_id ON t_payment_records_template (payment_id);
CREATE INDEX idx_payment_records_order_id ON t_payment_records_template (order_id);
CREATE INDEX idx_payment_records_user_id ON t_payment_records_template (user_id);
CREATE INDEX idx_payment_records_record_type ON t_payment_records_template (record_type);
CREATE INDEX idx_payment_records_created_date ON t_payment_records_template (created_date);
CREATE INDEX idx_payment_records_deleted ON t_payment_records_template (deleted);

CREATE TABLE IF NOT EXISTS t_refunds_template
(
    uuid                  VARCHAR(64) PRIMARY KEY,
    version_number        BIGINT                  DEFAULT 1,
    created_date          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    modified_date         TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    deleted               VARCHAR(64),

    refund_id             VARCHAR(64)    NOT NULL UNIQUE,
    order_id              VARCHAR(64)    NOT NULL,
    payment_id            VARCHAR(64)    NOT NULL,
    user_id               BIGINT         NOT NULL,

    refund_amount         NUMERIC(18, 2) NOT NULL,
    currency              VARCHAR(8)              DEFAULT 'CNY',

    refund_type           SMALLINT       NOT NULL,
    refund_reason         VARCHAR(256),

    refund_status         SMALLINT       NOT NULL DEFAULT 0,

    third_party_refund_no VARCHAR(128),
    third_party_trade_no  VARCHAR(128),

    apply_time            TIMESTAMP      NOT NULL,
    approve_time          TIMESTAMP,
    refund_time           TIMESTAMP,
    complete_time         TIMESTAMP,

    approver_id           VARCHAR(64),
    approver_name         VARCHAR(64),
    approve_remark        VARCHAR(512),

    refund_method         SMALLINT,
    bank_account          VARCHAR(128),
    bank_name             VARCHAR(128)
);

CREATE INDEX idx_refunds_refund_id ON t_refunds_template (refund_id);
CREATE INDEX idx_refunds_order_id ON t_refunds_template (order_id);
CREATE INDEX idx_refunds_payment_id ON t_refunds_template (payment_id);
CREATE INDEX idx_refunds_user_id ON t_refunds_template (user_id);
CREATE INDEX idx_refunds_refund_status ON t_refunds_template (refund_status);
CREATE INDEX idx_refunds_created_date ON t_refunds_template (created_date);
CREATE INDEX idx_refunds_refund_time ON t_refunds_template (refund_time);
CREATE INDEX idx_refunds_deleted ON t_refunds_template (deleted);
CREATE INDEX idx_refunds_user_status ON t_refunds_template (user_id, refund_status);


CREATE TABLE IF NOT EXISTS t_account_balance_template
(
    uuid                  VARCHAR(64) PRIMARY KEY,
    version_number        BIGINT         DEFAULT 1,
    created_date          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    modified_date         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    deleted               VARCHAR(64),

    user_id               BIGINT NOT NULL UNIQUE,

    available_balance     NUMERIC(18, 2) DEFAULT 0.00,
    frozen_balance        NUMERIC(18, 2) DEFAULT 0.00,
    total_balance         NUMERIC(18, 2) DEFAULT 0.00,
    currency              VARCHAR(8)     DEFAULT 'CNY',

    account_status        SMALLINT       DEFAULT 0,

    credit_limit          NUMERIC(18, 2) DEFAULT 0.00,
    last_transaction_time TIMESTAMP
);

CREATE INDEX idx_account_balance_user_id ON t_account_balance_template (user_id);
CREATE INDEX idx_account_balance_status ON t_account_balance_template (account_status);
CREATE INDEX idx_account_balance_deleted ON t_account_balance_template (deleted);

CREATE TABLE IF NOT EXISTS t_account_transactions_template
(
    uuid               VARCHAR(64) PRIMARY KEY,
    version_number     BIGINT                  DEFAULT 1,
    created_date       TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    modified_date      TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    deleted            VARCHAR(64),

    transaction_id     VARCHAR(64)    NOT NULL UNIQUE,
    user_id            BIGINT         NOT NULL,

    transaction_type   SMALLINT       NOT NULL,
    transaction_amount NUMERIC(18, 2) NOT NULL,
    balance_before     NUMERIC(18, 2) NOT NULL,
    balance_after      NUMERIC(18, 2) NOT NULL,
    currency           VARCHAR(8)              DEFAULT 'CNY',

    order_id           VARCHAR(64),
    payment_id         VARCHAR(64),
    refund_id          VARCHAR(64),

    transaction_status SMALLINT       NOT NULL DEFAULT 1,

    remark             VARCHAR(512),
    extra_data         TEXT
);

CREATE INDEX idx_account_tx_transaction_id ON t_account_transactions_template (transaction_id);
CREATE INDEX idx_account_tx_user_id ON t_account_transactions_template (user_id);
CREATE INDEX idx_account_tx_type ON t_account_transactions_template (transaction_type);
CREATE INDEX idx_account_tx_order_id ON t_account_transactions_template (order_id);
CREATE INDEX idx_account_tx_payment_id ON t_account_transactions_template (payment_id);
CREATE INDEX idx_account_tx_created_date ON t_account_transactions_template (created_date);
CREATE INDEX idx_account_tx_deleted ON t_account_transactions_template (deleted);
CREATE INDEX idx_account_tx_user_type_date
    ON t_account_transactions_template (user_id, transaction_type, created_date);

-- =========================
-- Users (broadcast: same table on all DBs, no sharding)
-- =========================
CREATE TABLE IF NOT EXISTS t_users_template
(
    uuid             VARCHAR(64) PRIMARY KEY,
    version_number   BIGINT                  DEFAULT 1,
    created_date     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    modified_date    TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    deleted          VARCHAR(64),

    user_id          BIGINT         NOT NULL UNIQUE,
    username         VARCHAR(64)    NOT NULL,
    email            VARCHAR(128),
    phone            VARCHAR(32),
    real_name        VARCHAR(64),
    id_card          VARCHAR(32),
    account_status   SMALLINT,
    account_balance  NUMERIC(18, 2),
    credit_score     SMALLINT,
    avatar_url       VARCHAR(512),
    gender           SMALLINT,
    birthday         DATE,
    address          TEXT
);

CREATE INDEX idx_users_user_id ON t_users_template (user_id);
CREATE INDEX idx_users_username ON t_users_template (username);
CREATE INDEX idx_users_deleted ON t_users_template (deleted);



