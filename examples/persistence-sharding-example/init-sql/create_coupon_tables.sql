-- TiDB-native schema for coupon sharding (single database, partitioned tables).
-- Replaces the old ShardingSphere layout (one_coupon_0 / one_coupon_1 + t_*_0..N).
-- Run this against TiDB (e.g. after docker compose up) with: mysql -h 127.0.0.1 -P 4000 -u root -p < schema-tidb.sql

CREATE DATABASE IF NOT EXISTS sharding_example;
USE sharding_example;

-- =============================================================================
-- Non-partitioned (global) tables
-- =============================================================================

CREATE TABLE `t_user` (
                          `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `shop_number` varchar(64)  DEFAULT NULL COMMENT 'Shop number',
    `username`    varchar(64)  DEFAULT NULL COMMENT 'Username',
    `password`    varchar(512) DEFAULT NULL COMMENT 'Password',
    `phone`       varchar(128) DEFAULT NULL COMMENT 'Phone number',
    `mail`        varchar(512) DEFAULT NULL COMMENT 'Email',
    `create_time` datetime     DEFAULT NULL COMMENT 'Created time',
    `update_time` datetime     DEFAULT NULL COMMENT 'Updated time',
    `del_flag`    tinyint(1) DEFAULT NULL COMMENT 'Delete flag 0: not deleted 1: deleted',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Merchant user table';

CREATE TABLE `t_coupon_task` (
                                 `id`                 bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `shop_number`        bigint(20) DEFAULT NULL COMMENT 'Shop number',
    `batch_id`           bigint(20) DEFAULT NULL COMMENT 'Batch ID',
    `task_name`          varchar(128) DEFAULT NULL COMMENT 'Coupon batch task name',
    `file_address`       varchar(512) DEFAULT NULL COMMENT 'File address',
    `send_num`           int(11) DEFAULT NULL COMMENT 'Number of coupons to send',
    `fail_file_address`  varchar(512) DEFAULT NULL COMMENT 'Failed-send user file address',
    `notify_type`        varchar(32)  DEFAULT NULL COMMENT 'Notification type',
    `coupon_template_id` bigint(20) DEFAULT NULL COMMENT 'Coupon template ID',
    `send_type`          tinyint(1) DEFAULT NULL COMMENT 'Send type 0: immediate 1: scheduled',
    `send_time`          datetime     DEFAULT NULL COMMENT 'Send time',
    `status`             tinyint(1) DEFAULT NULL COMMENT 'Status 0: pending 1: running 2: failed 3: success 4: cancelled',
    `completion_time`    datetime     DEFAULT NULL COMMENT 'Completion time',
    `create_time`        datetime     DEFAULT NULL COMMENT 'Created time',
    `operator_id`        bigint(20) DEFAULT NULL COMMENT 'Operator ID',
    `update_time`        datetime     DEFAULT NULL COMMENT 'Updated time',
    `del_flag`           tinyint(1) DEFAULT NULL COMMENT 'Delete flag 0: not deleted 1: deleted',
    PRIMARY KEY (`id`),
    KEY                  `idx_batch_id` (`batch_id`),
    KEY                  `idx_coupon_template_id` (`coupon_template_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Coupon template send task table';

CREATE TABLE `t_coupon_task_fail` (
                                      `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `batch_id`    bigint(20) DEFAULT NULL COMMENT 'Batch ID',
    `json_object` text COMMENT 'Failure content (JSON)',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Coupon task failure log';

CREATE TABLE `t_coupon_template_remind` (
                                            `user_id`            bigint(20) NOT NULL COMMENT 'User ID',
    `coupon_template_id` bigint(20) NOT NULL COMMENT 'Coupon template ID',
    `information`        bigint(20) DEFAULT NULL COMMENT 'Stored information',
    `shop_number`        bigint(20) DEFAULT NULL COMMENT 'Shop number',
    `start_time`         datetime DEFAULT NULL COMMENT 'Coupon availability start time',
    PRIMARY KEY (`user_id`, `coupon_template_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User coupon reminder subscription table';

-- =============================================================================
-- Partitioned tables (TiDB: one logical table, PARTITION BY HASH(sharding_key))
-- =============================================================================

-- Sharding key: user_id (id VARCHAR for PersistedObject; audit columns for PersistedObject base)
CREATE TABLE `t_coupon_settlement` (
                                       `id`             varchar(36) NOT NULL COMMENT 'ID',
    `order_id`       bigint(20)   DEFAULT NULL COMMENT 'Order ID',
    `user_id`        bigint(20)  NOT NULL COMMENT 'User ID',
    `coupon_id`      bigint(20)   DEFAULT NULL COMMENT 'Coupon ID',
    `status`         int(11)      DEFAULT NULL COMMENT 'Settlement status 0: locked 1: cancelled 2: paid 3: refunded',
    `create_time`    datetime     DEFAULT NULL COMMENT 'Created time',
    `update_time`    datetime     DEFAULT NULL COMMENT 'Updated time',
    `CREATED_DATE`   datetime     DEFAULT NULL,
    `MODIFIED_DATE`  datetime     DEFAULT NULL,
    `DELETED`        varchar(36)  DEFAULT NULL,
    `VERSION_NUMBER` bigint(20)   DEFAULT 1,
    `LOCKED`         bit(1)       DEFAULT b'0',
    `IS_DISABLED`    bit(1)       DEFAULT b'0',
    `IS_OUT_OF_SYNC` bit(1)       DEFAULT b'0',
    `entity_tag`     varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`, `user_id`),
    KEY           `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Coupon settlement table'
    PARTITION BY HASH(`user_id`) PARTITIONS 16;

-- Sharding key: shop_number
CREATE TABLE `t_coupon_template` (
                                     `id`               varchar(36) NOT NULL COMMENT 'ID',
    `name`             varchar(256) DEFAULT NULL COMMENT 'Coupon name',
    `shop_number`      bigint(20)  NOT NULL COMMENT 'Shop number',
    `source`           tinyint(1)   DEFAULT NULL COMMENT 'Coupon source 0: shop 1: platform',
    `target`           tinyint(1)   DEFAULT NULL COMMENT 'Target 0: product-specific 1: store-wide',
    `goods`            varchar(64)  DEFAULT NULL COMMENT 'Applicable goods code',
    `type`             tinyint(1)   DEFAULT NULL COMMENT 'Coupon type 0: instant discount 1: threshold discount 2: percentage discount',
    `valid_start_time` datetime     DEFAULT NULL COMMENT 'Validity start time',
    `valid_end_time`   datetime     DEFAULT NULL COMMENT 'Validity end time',
    `stock`            int(11)      DEFAULT NULL COMMENT 'Stock quantity',
    `receive_rule`     json         DEFAULT NULL COMMENT 'Receive rules (JSON)',
    `consume_rule`     json         DEFAULT NULL COMMENT 'Consume rules (JSON)',
    `status`           tinyint(1)   DEFAULT NULL COMMENT 'Coupon status 0: active 1: ended',
    `create_time`      datetime     DEFAULT NULL COMMENT 'Created time',
    `update_time`      datetime     DEFAULT NULL COMMENT 'Updated time',
    `del_flag`         tinyint(1)   DEFAULT NULL COMMENT 'Delete flag 0: not deleted 1: deleted',
    `CREATED_DATE`     datetime     DEFAULT NULL,
    `MODIFIED_DATE`    datetime     DEFAULT NULL,
    `DELETED`          varchar(36)  DEFAULT NULL,
    `VERSION_NUMBER`   bigint(20)   DEFAULT 1,
    `LOCKED`           bit(1)       DEFAULT b'0',
    `IS_DISABLED`      bit(1)       DEFAULT b'0',
    `IS_OUT_OF_SYNC`   bit(1)       DEFAULT b'0',
    `entity_tag`       varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`, `shop_number`),
    KEY                `idx_shop_number` (`shop_number`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Coupon template table'
    PARTITION BY HASH(`shop_number`) PARTITIONS 16;

-- Sharding key: shop_number
CREATE TABLE `t_coupon_template_log` (
                                         `id`                 varchar(36) NOT NULL COMMENT 'ID',
    `shop_number`        bigint(20)  NOT NULL COMMENT 'Shop number',
    `coupon_template_id` bigint(20)    DEFAULT NULL COMMENT 'Coupon template ID',
    `operator_id`        bigint(20)    DEFAULT NULL COMMENT 'Operator ID',
    `operation_log`      text COMMENT 'Operation log',
    `original_data`      varchar(1024) DEFAULT NULL COMMENT 'Original data',
    `modified_data`      varchar(1024) DEFAULT NULL COMMENT 'Modified data',
    `create_time`        datetime      DEFAULT NULL COMMENT 'Created time',
    `CREATED_DATE`       datetime      DEFAULT NULL,
    `MODIFIED_DATE`      datetime      DEFAULT NULL,
    `DELETED`            varchar(36)   DEFAULT NULL,
    `VERSION_NUMBER`     bigint(20)    DEFAULT 1,
    `LOCKED`             bit(1)        DEFAULT b'0',
    `IS_DISABLED`        bit(1)        DEFAULT b'0',
    `IS_OUT_OF_SYNC`     bit(1)        DEFAULT b'0',
    `entity_tag`         varchar(255)  DEFAULT NULL,
    PRIMARY KEY (`id`, `shop_number`),
    KEY                  `idx_shop_number` (`shop_number`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Coupon template operation log table'
    PARTITION BY HASH(`shop_number`) PARTITIONS 8;

-- Sharding key: user_id
CREATE TABLE `t_user_coupon` (
                                 `id`                 varchar(36) NOT NULL COMMENT 'ID',
    `user_id`            bigint(20)  NOT NULL COMMENT 'User ID',
    `coupon_template_id` bigint(20)   DEFAULT NULL COMMENT 'Coupon template ID',
    `receive_time`       datetime     DEFAULT NULL COMMENT 'Receive time',
    `receive_count`      int(3)       DEFAULT NULL COMMENT 'Receive count',
    `valid_start_time`   datetime     DEFAULT NULL COMMENT 'Validity start time',
    `valid_end_time`     datetime     DEFAULT NULL COMMENT 'Validity end time',
    `use_time`           datetime     DEFAULT NULL COMMENT 'Use time',
    `source`             tinyint(1)   DEFAULT NULL COMMENT 'Source 0: center 1: platform 2: shop',
    `status`             tinyint(1)   DEFAULT NULL COMMENT 'Status 0: unused 1: locked 2: used 3: expired 4: revoked',
    `create_time`        datetime     DEFAULT NULL COMMENT 'Created time',
    `update_time`        datetime     DEFAULT NULL COMMENT 'Updated time',
    `del_flag`           tinyint(1)   DEFAULT NULL COMMENT 'Delete flag 0: not deleted 1: deleted',
    `CREATED_DATE`       datetime     DEFAULT NULL,
    `MODIFIED_DATE`      datetime     DEFAULT NULL,
    `DELETED`            varchar(36)  DEFAULT NULL,
    `VERSION_NUMBER`     bigint(20)   DEFAULT 1,
    `LOCKED`             bit(1)       DEFAULT b'0',
    `IS_DISABLED`        bit(1)       DEFAULT b'0',
    `IS_OUT_OF_SYNC`     bit(1)       DEFAULT b'0',
    `entity_tag`         varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`, `user_id`),
    UNIQUE KEY `idx_user_id_coupon_template_receive_count` (`user_id`,`coupon_template_id`,`receive_count`),
    KEY                  `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User coupon table'
    PARTITION BY HASH(`user_id`) PARTITIONS 32;

-- Sharding key: user_id
CREATE TABLE `t_user_coupon_log` (
                                     `id`             varchar(36) NOT NULL COMMENT 'ID',
    `user_id`        bigint(20)  NOT NULL COMMENT 'User ID',
    `coupon_id`      bigint(20)  NOT NULL COMMENT 'Coupon ID',
    `operation_log`  text COMMENT 'Operation log',
    `create_time`    datetime     DEFAULT NULL COMMENT 'Created time',
    `CREATED_DATE`   datetime     DEFAULT NULL,
    `MODIFIED_DATE`  datetime     DEFAULT NULL,
    `DELETED`        varchar(36)  DEFAULT NULL,
    `VERSION_NUMBER` bigint(20)   DEFAULT 1,
    `LOCKED`         bit(1)       DEFAULT b'0',
    `IS_DISABLED`    bit(1)       DEFAULT b'0',
    `IS_OUT_OF_SYNC` bit(1)       DEFAULT b'0',
    `entity_tag`     varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`, `user_id`),
    KEY             `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User coupon operation log table'
    PARTITION BY HASH(`user_id`) PARTITIONS 32;
