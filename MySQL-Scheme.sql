-- ============================================================
-- 订单系统表结构 DDL
-- 符合项目代码编写规范
-- ============================================================

-- ------------------------------------------------------------
-- 1. 订单表（order）- 聚合根
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_aggr`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`       VARCHAR(64)    NOT NULL COMMENT '订单编号',
    `customer_id`    VARCHAR(64)    NOT NULL COMMENT '客户ID',
    `customer_name`  VARCHAR(128)   NOT NULL COMMENT '客户姓名',
    `status`         VARCHAR(32)    NOT NULL COMMENT '订单状态：CREATED-已创建, PAID-已支付, CANCELLED-已取消, SHIPPED-已发货, COMPLETED-已完成',
    `total_amount`   DECIMAL(18, 2) NOT NULL COMMENT '订单总金额',
    `currency`       VARCHAR(16)    NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
    `payment_method` VARCHAR(32)             DEFAULT NULL COMMENT '支付方式',
    `remark`         VARCHAR(512)            DEFAULT NULL COMMENT '备注',

    -- 审计字段（必须）
    `create_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`    TIMESTAMP      NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`    VARCHAR(64)             DEFAULT NULL COMMENT '创建人ID',
    `update_user`    VARCHAR(64)             DEFAULT NULL COMMENT '更新人ID',
    `delete_user`    VARCHAR(64)             DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_order_no` (`order_no`),
    KEY `idx_order_customer_id` (`customer_id`),
    KEY `idx_order_status` (`status`),
    KEY `idx_order_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单表-聚合根';

-- ------------------------------------------------------------
-- 2. 订单项表（order_item）- 实体
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_item`
(
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
    `order_id`     BIGINT         NOT NULL COMMENT '订单ID',
    `product_id`   VARCHAR(64)    NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(256)   NOT NULL COMMENT '商品名称',
    `sku_code`     VARCHAR(64)    NOT NULL COMMENT 'SKU编码',
    `unit_price`   DECIMAL(18, 2) NOT NULL COMMENT '单价',
    `currency`     VARCHAR(16)    NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
    `quantity`     INT            NOT NULL COMMENT '数量',
    `subtotal`     DECIMAL(18, 2) NOT NULL COMMENT '小计金额',

    -- 审计字段（必须）
    `create_time`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`  TIMESTAMP      NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`  VARCHAR(64)             DEFAULT NULL COMMENT '创建人ID',
    `update_user`  VARCHAR(64)             DEFAULT NULL COMMENT '更新人ID',
    `delete_user`  VARCHAR(64)             DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    KEY `idx_order_item_order_id` (`order_id`),
    KEY `idx_order_item_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单项表-实体';

-- ------------------------------------------------------------
-- 3. 订单地址表（order_address）- 值对象
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_address`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `order_id`       BIGINT       NOT NULL COMMENT '订单ID',
    `province`       VARCHAR(64)  NOT NULL COMMENT '省份',
    `city`           VARCHAR(64)  NOT NULL COMMENT '城市',
    `district`       VARCHAR(64)           DEFAULT NULL COMMENT '区县',
    `detail_address` VARCHAR(512) NOT NULL COMMENT '详细地址',
    `postal_code`    VARCHAR(16)           DEFAULT NULL COMMENT '邮政编码',

    -- 审计字段（必须）
    `create_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`    TIMESTAMP    NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`    VARCHAR(64)           DEFAULT NULL COMMENT '创建人ID',
    `update_user`    VARCHAR(64)           DEFAULT NULL COMMENT '更新人ID',
    `delete_user`    VARCHAR(64)           DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_address_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单地址表-值对象';

-- ------------------------------------------------------------
-- 4. 订单联系信息表（order_contact_info）- 值对象
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_contact_info`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '联系信息ID',
    `order_id`      BIGINT       NOT NULL COMMENT '订单ID',
    `contact_name`  VARCHAR(128) NOT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(32)  NOT NULL COMMENT '联系人电话',
    `contact_email` VARCHAR(128)          DEFAULT NULL COMMENT '联系人邮箱',

    -- 审计字段（必须）
    `create_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`   TIMESTAMP    NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`   VARCHAR(64)           DEFAULT NULL COMMENT '创建人ID',
    `update_user`   VARCHAR(64)           DEFAULT NULL COMMENT '更新人ID',
    `delete_user`   VARCHAR(64)           DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_contact_info_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单联系信息表-值对象';

-- ------------------------------------------------------------
-- 5. 支付记录表（payment）- 支付记录
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `payment`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `payment_no`     VARCHAR(64)    NOT NULL COMMENT '支付编号',
    `order_id`       BIGINT         NOT NULL COMMENT '订单ID',
    `order_no`       VARCHAR(64)    NOT NULL COMMENT '订单编号',
    `payment_method` VARCHAR(32)    NOT NULL COMMENT '支付方式：ALIPAY-支付宝, WECHAT-微信, STRIPE-Stripe',
    `amount`         DECIMAL(18, 2) NOT NULL COMMENT '支付金额',
    `currency`       VARCHAR(16)    NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
    `status`         VARCHAR(32)    NOT NULL COMMENT '支付状态：PENDING-待支付, SUCCESS-成功, FAILED-失败, REFUNDED-已退款',
    `transaction_id` VARCHAR(128)            DEFAULT NULL COMMENT '第三方交易ID',
    `failed_reason`  VARCHAR(512)            DEFAULT NULL COMMENT '失败原因',

    -- 审计字段（必须）
    `create_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`    TIMESTAMP      NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`    VARCHAR(64)             DEFAULT NULL COMMENT '创建人ID',
    `update_user`    VARCHAR(64)             DEFAULT NULL COMMENT '更新人ID',
    `delete_user`    VARCHAR(64)             DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_payment_no` (`payment_no`),
    KEY `idx_payment_order_id` (`order_id`),
    KEY `idx_payment_order_no` (`order_no`),
    KEY `idx_payment_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='支付记录表';