-- =============================================
-- DDL-MySQL.sql
-- 数据库表结构定义
-- Generated based on entity classes in the project
-- =============================================

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id`      BIGINT        DEFAULT NULL COMMENT '订单ID（对应聚合根的业务ID）',
    `customer_id`   BIGINT        DEFAULT NULL COMMENT '客户ID',
    `total_amount`  DECIMAL(19, 2) DEFAULT NULL COMMENT '订单总金额',
    `currency`      VARCHAR(32)   DEFAULT NULL COMMENT '币种',
    `status`        VARCHAR(32)   DEFAULT NULL COMMENT '订单状态',
    `shipping_address` VARCHAR(512) DEFAULT NULL COMMENT '收货地址',
    `phone_number`  VARCHAR(64)   DEFAULT NULL COMMENT '联系电话',
    `payment_time`  TIMESTAMP     NULL     DEFAULT NULL COMMENT '支付时间',
    `shipping_time` TIMESTAMP     NULL     DEFAULT NULL COMMENT '发货时间',
    `completed_time` TIMESTAMP    NULL     DEFAULT NULL COMMENT '完成时间',
    `cancelled_time` TIMESTAMP    NULL     DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(512)  DEFAULT NULL COMMENT '取消原因',
    `version`       BIGINT        DEFAULT 0 COMMENT '版本号（乐观锁）',
    `create_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`   TIMESTAMP     NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`   VARCHAR(64)   DEFAULT NULL COMMENT '创建人ID',
    `update_user`   VARCHAR(64)   DEFAULT NULL COMMENT '更新人ID',
    `delete_user`   VARCHAR(64)   DEFAULT NULL COMMENT '删除人ID',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_customer_id` (`customer_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id`      BIGINT        DEFAULT NULL COMMENT '订单ID',
    `product_id`    VARCHAR(64)   DEFAULT NULL COMMENT '产品ID',
    `product_name`  VARCHAR(256)  DEFAULT NULL COMMENT '产品名称',
    `unit_price`    DECIMAL(19, 2) DEFAULT NULL COMMENT '单价',
    `currency`      VARCHAR(32)   DEFAULT NULL COMMENT '币种',
    `quantity`      INT           DEFAULT NULL COMMENT '数量',
    `subtotal`      DECIMAL(19, 2) DEFAULT NULL COMMENT '小计',
    `create_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`   TIMESTAMP     NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`   VARCHAR(64)   DEFAULT NULL COMMENT '创建人ID',
    `update_user`   VARCHAR(64)   DEFAULT NULL COMMENT '更新人ID',
    `delete_user`   VARCHAR(64)   DEFAULT NULL COMMENT '删除人ID',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `business_type` VARCHAR(32)   DEFAULT NULL COMMENT '业务类型',
    `method`        VARCHAR(256)  DEFAULT NULL COMMENT '方法名',
    `arg_string`    TEXT          DEFAULT NULL COMMENT '参数',
    `result_string` TEXT          DEFAULT NULL COMMENT '结果',
    `thread_name`   VARCHAR(128)  DEFAULT NULL COMMENT '线程名',
    `exception`     TEXT          DEFAULT NULL COMMENT '异常信息',
    `start_time`    TIMESTAMP     NULL     DEFAULT NULL COMMENT '开始时间',
    `end_time`      TIMESTAMP     NULL     DEFAULT NULL COMMENT '结束时间',
    `create_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`   TIMESTAMP     NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`   VARCHAR(64)   DEFAULT NULL COMMENT '创建人ID',
    `update_user`   VARCHAR(64)   DEFAULT NULL COMMENT '更新人ID',
    `delete_user`   VARCHAR(64)   DEFAULT NULL COMMENT '删除人ID',
    PRIMARY KEY (`id`),
    INDEX `idx_business_type` (`business_type`),
    INDEX `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日志表';

-- ----------------------------
-- Table structure for file_metadata
-- ----------------------------
DROP TABLE IF EXISTS `file_metadata`;
CREATE TABLE `file_metadata` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id`       VARCHAR(64)   NOT NULL COMMENT '文件唯一标识',
    `md5`           CHAR(32)      DEFAULT NULL COMMENT '文件MD5值',
    `content_type`  VARCHAR(128)  DEFAULT NULL COMMENT '文件MIME类型',
    `size`          BIGINT        DEFAULT NULL COMMENT '文件大小（字节）',
    `url`           VARCHAR(512)  DEFAULT NULL COMMENT '文件访问URL',
    `url_expire`    TIMESTAMP     NULL     DEFAULT NULL COMMENT 'URL过期时间',
    `path`          VARCHAR(512)  DEFAULT NULL COMMENT '文件存储路径',
    `create_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`   TIMESTAMP     NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`   VARCHAR(64)   DEFAULT NULL COMMENT '创建人ID',
    `update_user`   VARCHAR(64)   DEFAULT NULL COMMENT '更新人ID',
    `delete_user`   VARCHAR(64)   DEFAULT NULL COMMENT '删除人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_id` (`file_id`),
    INDEX `idx_md5` (`md5`),
    INDEX `idx_content_type` (`content_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件元数据表';

-- ----------------------------
-- Table structure for file_business
-- ----------------------------
DROP TABLE IF EXISTS `file_business`;
CREATE TABLE `file_business` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id`       VARCHAR(64)   NOT NULL COMMENT '文件ID',
    `name`          VARCHAR(256)  DEFAULT NULL COMMENT '文件业务名称',
    `type`          VARCHAR(32)   DEFAULT NULL COMMENT '业务类型',
    `business_id`   VARCHAR(64)   DEFAULT NULL COMMENT '业务唯一标识',
    `usage`         VARCHAR(128)  DEFAULT NULL COMMENT '使用场景',
    `sort`          INT           DEFAULT 0 COMMENT '排序序号',
    `remark`        VARCHAR(512)  DEFAULT NULL COMMENT '备注',
    `create_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`   TIMESTAMP     NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`   VARCHAR(64)   DEFAULT NULL COMMENT '创建人ID',
    `update_user`   VARCHAR(64)   DEFAULT NULL COMMENT '更新人ID',
    `delete_user`   VARCHAR(64)   DEFAULT NULL COMMENT '删除人ID',
    PRIMARY KEY (`id`),
    INDEX `idx_file_id` (`file_id`),
    INDEX `idx_business_id` (`business_id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件业务关联表';

-- ----------------------------
-- Table structure for event_publish
-- ----------------------------
DROP TABLE IF EXISTS `event_publish`;
CREATE TABLE `event_publish` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_id`        VARCHAR(64)   NOT NULL COMMENT '事件唯一标识（UUID）',
    `aggregate_id`    VARCHAR(64)   DEFAULT NULL COMMENT '聚合根ID',
    `aggregate_type`  VARCHAR(64)   DEFAULT NULL COMMENT '聚合根类型（如Order、Customer）',
    `priority`        VARCHAR(32)   DEFAULT NULL COMMENT '优先级：HIGH(高)/LOW(低)',
    `occurred_on`     TIMESTAMP     NULL     DEFAULT NULL COMMENT '事件发生时间',
    `prev_id`         BIGINT        DEFAULT NULL COMMENT '前驱事件ID',
    `step`            INT           DEFAULT NULL COMMENT '步骤，第几步',
    `source`          VARCHAR(128)  DEFAULT NULL COMMENT '事件来源',
    `type`            VARCHAR(128)  DEFAULT NULL COMMENT '事件类型',
    `data`            TEXT          DEFAULT NULL COMMENT '事件载荷（JSON格式）',
    `status`          VARCHAR(32)   DEFAULT NULL COMMENT '事件状态：CREATED(已创建)/READY(就绪)/PUBLISHED(已发布)',
    `max_retry_times` INT           DEFAULT NULL COMMENT '最大重试次数',
    `version`         BIGINT        DEFAULT 0 COMMENT '版本号（乐观锁）',
    `create_time`     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`     TIMESTAMP     NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`     VARCHAR(64)   DEFAULT NULL COMMENT '创建人ID',
    `update_user`     VARCHAR(64)   DEFAULT NULL COMMENT '更新人ID',
    `delete_user`     VARCHAR(64)   DEFAULT NULL COMMENT '删除人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`),
    INDEX `idx_aggregate_id` (`aggregate_id`),
    INDEX `idx_aggregate_type` (`aggregate_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_occurred_on` (`occurred_on`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件发布表';

-- ----------------------------
-- Table structure for event_consume
-- ----------------------------
DROP TABLE IF EXISTS `event_consume`;
CREATE TABLE `event_consume` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_id`        VARCHAR(64)   NOT NULL COMMENT '事件ID，关联event_publish.event_id',
    `priority`        VARCHAR(32)   DEFAULT NULL COMMENT '优先级：HIGH(高)/LOW(低)',
    `idempotent_key`  VARCHAR(128)  DEFAULT NULL COMMENT '幂等键（防止重复消费）',
    `consumer_group`  VARCHAR(128)  DEFAULT NULL COMMENT '消费者组',
    `consumer_name`   VARCHAR(128)  DEFAULT NULL COMMENT '消费者名称',
    `consume_status`  VARCHAR(32)   DEFAULT NULL COMMENT '消费状态：READY(准备消费)/CONSUMED(已消费)/RETRY(重试中)/FAILED(失败)',
    `consume_time`    TIMESTAMP     NULL     DEFAULT NULL COMMENT '消费开始时间',
    `complete_time`   TIMESTAMP     NULL     DEFAULT NULL COMMENT '消费完成时间',
    `next_retry_time` TIMESTAMP     NULL     DEFAULT NULL COMMENT '下次重试时间',
    `retry_times`     INT           DEFAULT 0 COMMENT '当前重试次数',
    `max_retry_times` INT           DEFAULT NULL COMMENT '最大重试次数',
    `error_message`   TEXT          DEFAULT NULL COMMENT '错误信息',
    `version`         BIGINT        DEFAULT 0 COMMENT '版本号（乐观锁）',
    `create_time`     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`     TIMESTAMP     NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`     VARCHAR(64)   DEFAULT NULL COMMENT '创建人ID',
    `update_user`     VARCHAR(64)   DEFAULT NULL COMMENT '更新人ID',
    `delete_user`     VARCHAR(64)   DEFAULT NULL COMMENT '删除人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_idempotent_key` (`idempotent_key`),
    INDEX `idx_event_id` (`event_id`),
    INDEX `idx_consumer_group` (`consumer_group`),
    INDEX `idx_consume_status` (`consume_status`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_next_retry_time` (`next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件消费表';
