-- -----------------------------------------------------
-- 统一字段规范说明
-- 1. 所有主键ID统一为：BIGINT NOT NULL AUTO_INCREMENT
-- 2. 用户ID字段统一为：VARCHAR(64) DEFAULT NULL
-- 3. 移除所有显示宽度指定（INT(11)、BIGINT(20)、BIGINT(32)等）
-- 4. UUID/唯一标识：VARCHAR(64)
-- 5. 业务类型：VARCHAR(32)
-- 6. 服务名称/组名：VARCHAR(128)
-- 7. 状态字段：VARCHAR(32)
-- 8. 文件路径/URL：VARCHAR(512)
-- 9. MD5/哈希值：CHAR(32)
-- 10. 业务ID：VARCHAR(64)
-- 11. 枚举字段统一用VARCHAR(32)，不使用ENUM类型
-- 12. 索引命名规范：uk_{字段名}（唯一索引），idx_{字段名1}_{字段名2}（复合索引）
-- -----------------------------------------------------

-- -----------------------------------------------------
-- 日志表
-- 记录系统操作日志、方法调用日志等
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `log`
(
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `create_time`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`   TIMESTAMP   NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`   VARCHAR(64)          DEFAULT NULL COMMENT '创建人ID',
    `update_user`   VARCHAR(64)          DEFAULT NULL COMMENT '更新人ID',
    `delete_user`   VARCHAR(64) NULL     DEFAULT NULL COMMENT '删除人ID',
    `version`       BIGINT               DEFAULT 0 COMMENT '版本号（乐观锁）',
    `business_type` VARCHAR(32)          DEFAULT NULL COMMENT '业务类型',
    `method`        VARCHAR(256)         DEFAULT NULL COMMENT '方法名',
    `arg_string`    TEXT COMMENT '参数',
    `result_string` TEXT COMMENT '结果',
    `thread_name`   VARCHAR(128)         DEFAULT NULL COMMENT '线程名',
    `exception`     TEXT COMMENT '异常信息',
    `start_time`    TIMESTAMP   NULL     DEFAULT NULL COMMENT '开始时间',
    `end_time`      TIMESTAMP   NULL     DEFAULT NULL COMMENT '结束时间',
    PRIMARY KEY (`id`),
    KEY `idx_business_type` (`business_type`) COMMENT '业务类型索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='日志表';

-- -----------------------------------------------------
-- 事件发布表
-- 用于持久化领域事件，支持事件溯源和重放
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `event_publish`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_id`        VARCHAR(64)  NOT NULL COMMENT '事件唯一标识（UUID）',
    `aggregate_id`    VARCHAR(64)  NOT NULL COMMENT '聚合根ID',
    `aggregate_type`  VARCHAR(128) NOT NULL COMMENT '聚合根类型（如Order、Customer）',
    `priority`        VARCHAR(32)  NOT NULL DEFAULT 'LOW' COMMENT '优先级：HIGH(高)/LOW(低)',
    `occurred_on`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件发生时间',
    `prev_id`         BIGINT                DEFAULT NULL COMMENT '前驱事件ID',
    `step`            INT                   DEFAULT 0 COMMENT '步骤，第几步',
    `source`          VARCHAR(128) NOT NULL COMMENT '事件来源',
    `type`            VARCHAR(128) NOT NULL COMMENT '事件类型',
    `data`            JSON         NOT NULL COMMENT '事件载荷（JSON格式）',
    `status`          VARCHAR(32)  NOT NULL DEFAULT 'CREATED' COMMENT '事件状态：CREATED(已创建)/READY(就绪)/PUBLISHED(已发布)',
    `max_retry_times` INT                   DEFAULT 3 COMMENT '最大重试次数',
    `create_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`     VARCHAR(64)           DEFAULT NULL COMMENT '创建人ID',
    `update_user`     VARCHAR(64)           DEFAULT NULL COMMENT '更新人ID',
    `version`         BIGINT                DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`) COMMENT '事件ID唯一索引',
    KEY `idx_aggregate` (`aggregate_id`, `aggregate_type`) COMMENT '聚合根索引',
    KEY `idx_type` (`type`) COMMENT '事件类型索引',
    KEY `idx_status_priority` (`status`, `priority`, `occurred_on`) COMMENT '状态-优先级-时间复合索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='事件发布表';

-- -----------------------------------------------------
-- 事件消费表
-- 用于跟踪事件的消费状态，支持重试和幂等
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `event_consume`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_id`        VARCHAR(64)  NOT NULL COMMENT '事件ID，关联event_publish.event_id',
    `priority`        VARCHAR(32)  NOT NULL DEFAULT 'LOW' COMMENT '优先级：HIGH(高)/LOW(低)',
    `idempotent_key`  VARCHAR(128) NOT NULL COMMENT '幂等键（防止重复消费）',
    `consumer_group`  VARCHAR(128) NOT NULL COMMENT '消费者组',
    `consumer_name`   VARCHAR(128) NOT NULL COMMENT '消费者名称',
    `consume_status`  VARCHAR(32)  NOT NULL DEFAULT 'READY' COMMENT '消费状态：READY(准备消费)/CONSUMED(已消费)/RETRY(重试中)/FAILED(失败)',
    `consume_time`    TIMESTAMP    NULL     DEFAULT NULL COMMENT '消费开始时间',
    `complete_time`   TIMESTAMP    NULL     DEFAULT NULL COMMENT '消费完成时间',
    `next_retry_time` TIMESTAMP    NULL     DEFAULT NULL COMMENT '下次重试时间',
    `retry_times`     INT                   DEFAULT 0 COMMENT '当前重试次数',
    `max_retry_times` INT                   DEFAULT 3 COMMENT '最大重试次数',
    `error_message`   TEXT COMMENT '错误信息',
    `create_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`     VARCHAR(64)           DEFAULT NULL COMMENT '创建人ID',
    `update_user`     VARCHAR(64)           DEFAULT NULL COMMENT '更新人ID',
    `version`         BIGINT                DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_consumer` (`event_id`, `consumer_group`, `consumer_name`) COMMENT '事件-消费者唯一索引',
    UNIQUE KEY `uk_idempotent` (`idempotent_key`, `consumer_group`) COMMENT '幂等键唯一索引',
    KEY `idx_status_priority` (`consume_status`, `priority`, `create_time`) COMMENT '状态-优先级-时间复合索引（用于定时任务扫描）',
    KEY `idx_next_retry` (`consume_status`, `next_retry_time`) COMMENT '下次重试时间索引',
    KEY `idx_event_id` (`event_id`) COMMENT '事件ID索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='事件消费表';

-- -----------------------------------------------------
-- 文件元数据表
-- 存储文件的基础元数据信息
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `file_metadata`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `create_time`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`  VARCHAR(64)           DEFAULT NULL COMMENT '创建人ID',
    `update_user`  VARCHAR(64)           DEFAULT NULL COMMENT '更新人ID',
    `md5`          CHAR(32)     NOT NULL COMMENT '文件MD5值',
    `content_type` VARCHAR(64)  NOT NULL COMMENT '文件MIME类型',
    `size`         BIGINT       NOT NULL COMMENT '文件大小（字节）',
    `url`          VARCHAR(512)          DEFAULT NULL COMMENT '文件访问URL',
    `url_expire`   TIMESTAMP    NULL     DEFAULT NULL COMMENT 'URL过期时间',
    `path`         VARCHAR(512) NOT NULL COMMENT '文件存储路径',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_md5` (`md5`) COMMENT 'MD5索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='文件元数据表';

-- -----------------------------------------------------
-- 文件业务表
-- 存储文件的业务关联信息
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `file_business`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `create_time`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`  VARCHAR(64)           DEFAULT NULL COMMENT '创建人ID',
    `update_user`  VARCHAR(64)           DEFAULT NULL COMMENT '更新人ID',
    `file_meta_id` VARCHAR(64)  NOT NULL COMMENT '文件ID',
    `business_id`  VARCHAR(64)  NOT NULL COMMENT '业务ID',
    `name`         VARCHAR(128) NOT NULL COMMENT '文件业务名称',
    `type`         VARCHAR(64)  NOT NULL COMMENT '业务类型',
    `usage`        VARCHAR(64)           DEFAULT NULL COMMENT '使用场景',
    `sort`         INT                   DEFAULT 0 COMMENT '排序序号',
    `remark`       VARCHAR(512)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_file_id` (file_meta_id) COMMENT '文件ID索引',
    KEY `idx_type_usage` (`type`, `usage`) COMMENT '业务类型-使用场景索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='文件业务关联表';