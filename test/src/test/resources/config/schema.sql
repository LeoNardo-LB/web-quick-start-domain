-- ============================================================
-- H2测试数据库表结构 DDL
-- 仅用于集成测试环境（test模块专用）
-- ============================================================

-- 事件发布表
CREATE TABLE IF NOT EXISTS event_publish
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id        VARCHAR(64),
    aggregate_id    VARCHAR(64),
    aggregate_type  VARCHAR(128),
    priority        VARCHAR(32),
    occurred_on     TIMESTAMP,
    prev_id         BIGINT,
    step            INT,
    source          VARCHAR(128),
    type            VARCHAR(128),
    data            CLOB,
    status          VARCHAR(32),
    max_retry_times INT,
    create_time     TIMESTAMP,
    update_time     TIMESTAMP,
    create_user     VARCHAR(64),
    update_user     VARCHAR(64),
    version         BIGINT
);

-- 事件消费表
CREATE TABLE IF NOT EXISTS event_consume
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id        VARCHAR(64),
    priority        VARCHAR(32),
    idempotent_key  VARCHAR(128),
    consumer_group  VARCHAR(128),
    consumer_name   VARCHAR(128),
    consume_status  VARCHAR(32),
    consume_time    TIMESTAMP,
    complete_time   TIMESTAMP,
    next_retry_time TIMESTAMP,
    retry_times     INT,
    max_retry_times INT,
    error_message   CLOB,
    create_time     TIMESTAMP,
    update_time     TIMESTAMP,
    create_user     VARCHAR(64),
    update_user     VARCHAR(64),
    version         BIGINT
);

-- 订单聚合根表
CREATE TABLE IF NOT EXISTS order_aggr
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no       VARCHAR(64),
    customer_id    VARCHAR(64),
    customer_name  VARCHAR(128),
    status         VARCHAR(32),
    total_amount   DECIMAL(18, 2),
    currency       VARCHAR(16),
    payment_method VARCHAR(32),
    payment_time   TIMESTAMP,
    remark         VARCHAR(512),
    create_time    TIMESTAMP,
    update_time    TIMESTAMP,
    delete_time    TIMESTAMP,
    create_user    VARCHAR(64),
    update_user    VARCHAR(64),
    delete_user    VARCHAR(64)
);

-- 订单项表
CREATE TABLE IF NOT EXISTS order_item
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id     BIGINT,
    product_id   VARCHAR(64),
    product_name VARCHAR(256),
    sku_code     VARCHAR(64),
    unit_price   DECIMAL(18, 2),
    currency     VARCHAR(16),
    quantity     INT,
    subtotal     DECIMAL(18, 2),
    create_time  TIMESTAMP,
    update_time  TIMESTAMP,
    delete_time  TIMESTAMP,
    create_user  VARCHAR(64),
    update_user  VARCHAR(64),
    delete_user  VARCHAR(64)
);

-- 订单地址表
CREATE TABLE IF NOT EXISTS order_address
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id       BIGINT,
    province       VARCHAR(64),
    city           VARCHAR(64),
    district       VARCHAR(64),
    detail_address VARCHAR(512),
    postal_code    VARCHAR(16),
    create_time    TIMESTAMP,
    update_time    TIMESTAMP,
    delete_time    TIMESTAMP,
    create_user    VARCHAR(64),
    update_user    VARCHAR(64),
    delete_user    VARCHAR(64)
);

-- 订单联系信息表
CREATE TABLE IF NOT EXISTS order_contact_info
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id      BIGINT,
    contact_name  VARCHAR(128),
    contact_phone VARCHAR(32),
    contact_email VARCHAR(128),
    create_time   TIMESTAMP,
    update_time   TIMESTAMP,
    delete_time   TIMESTAMP,
    create_user   VARCHAR(64),
    update_user   VARCHAR(64),
    delete_user   VARCHAR(64)
);