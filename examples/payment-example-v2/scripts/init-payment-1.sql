-- =====================================
-- Initialize Payment Database Shard 1
-- =====================================

-- =========================
-- Payments (16 shards)
-- =========================

CREATE TABLE IF NOT EXISTS t_payments_0
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_1
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_2
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_3
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_4
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_5
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_6
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_7
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_8
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_9
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_10
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_11
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_12
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_13
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_14
(
    LIKE t_payments_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payments_15
(
    LIKE t_payments_template INCLUDING ALL
);

-- =========================
-- Payment Records (16 shards)
-- =========================

CREATE TABLE IF NOT EXISTS t_payment_records_0
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_1
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_2
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_3
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_4
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_5
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_6
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_7
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_8
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_9
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_10
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_11
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_12
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_13
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_14
(
    LIKE t_payment_records_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_payment_records_15
(
    LIKE t_payment_records_template INCLUDING ALL
);

-- =========================
-- Refunds (8 shards)
-- =========================

CREATE TABLE IF NOT EXISTS t_refunds_0
(
    LIKE t_refunds_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_refunds_1
(
    LIKE t_refunds_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_refunds_2
(
    LIKE t_refunds_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_refunds_3
(
    LIKE t_refunds_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_refunds_4
(
    LIKE t_refunds_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_refunds_5
(
    LIKE t_refunds_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_refunds_6
(
    LIKE t_refunds_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_refunds_7
(
    LIKE t_refunds_template INCLUDING ALL
);

-- =========================
-- Account Balance (single table)
-- =========================

CREATE TABLE IF NOT EXISTS t_account_balance
(
    LIKE t_account_balance_template INCLUDING ALL
);

-- =========================
-- Account Transactions (32 shards)
-- =========================

CREATE TABLE IF NOT EXISTS t_account_transactions_0
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_1
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_2
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_3
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_4
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_5
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_6
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_7
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_8
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_9
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_10
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_11
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_12
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_13
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_14
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_15
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_16
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_17
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_18
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_19
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_20
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_21
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_22
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_23
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_24
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_25
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_26
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_27
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_28
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_29
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_30
(
    LIKE t_account_transactions_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_account_transactions_31
(
    LIKE t_account_transactions_template INCLUDING ALL
);
