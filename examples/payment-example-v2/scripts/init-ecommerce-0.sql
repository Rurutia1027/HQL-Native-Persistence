-- Connect to database ecommerce_0 before running this script
-- \c ecommerce_0   (psql client)

-- =========================
-- Orders Shards (16 tables)
-- =========================

CREATE TABLE IF NOT EXISTS t_orders_0
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_1
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_2
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_3
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_4
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_5
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_6
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_7
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_8
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_9
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_10
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_11
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_12
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_13
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_14
(
    LIKE t_orders_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_orders_15
(
    LIKE t_orders_template INCLUDING ALL
);

-- =============================
-- Order Items Shards (16 tables)
-- =============================

CREATE TABLE IF NOT EXISTS t_order_items_0
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_1
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_2
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_3
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_4
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_5
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_6
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_7
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_8
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_9
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_10
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_11
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_12
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_13
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_14
(
    LIKE t_order_items_template INCLUDING ALL
);
CREATE TABLE IF NOT EXISTS t_order_items_15
(
    LIKE t_order_items_template INCLUDING ALL
);

-- =========================
-- Users (broadcast table: same on all DBs)
-- =========================
CREATE TABLE IF NOT EXISTS t_users
(
    LIKE t_users_template INCLUDING ALL
);
