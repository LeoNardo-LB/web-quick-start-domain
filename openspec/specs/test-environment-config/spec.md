# test-environment-config Specification

## Purpose

测试环境配置规范，确保测试代码使用独立配置文件，与生产环境解耦。

## Requirements

### Requirement: 测试环境日志配置

测试环境必须使用独立的日志配置文件，不依赖 Spring Profile 机制。

#### Scenario: 单元测试日志配置加载

- **WHEN** 运行单元测试（不启动 Spring 上下文）
- **THEN** 系统必须自动加载 `logback-test.xml` 配置文件
- **THEN** 必须配置所有必需的 appender（CONSOLE、FILE、ASYNC_FILE、ASYNC_CURRENT、ERROR_FILE、AUDIT_FILE）
- **THEN** 必须配置敏感信息脱敏转换器（DesensitizingConverter）

#### Scenario: 测试日志配置与生产配置解耦

- **WHEN** 修改测试环境日志配置
- **THEN** 不应影响生产环境的 `logback-spring.xml` 配置
- **THEN** 测试配置文件必须位于 `test/src/test/resources/` 目录

---

### Requirement: 测试环境数据库配置

测试环境必须使用内存数据库（H2）模拟生产数据库（MySQL）行为。

#### Scenario: H2 数据库兼容模式

- **WHEN** 配置测试环境数据库
- **THEN** 必须使用 H2 的 MySQL 兼容模式（`MODE=MySQL`）
- **THEN** 必须启用大小写不敏感标识符（`CASE_INSENSITIVE_IDENTIFIERS=TRUE`）
- **THEN** 必须配置 `DB_CLOSE_DELAY=-1` 保持数据库连接

#### Scenario: 测试数据源配置

- **WHEN** 配置测试环境数据源
- **THEN** JDBC URL 必须为 `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE`
- **THEN** 驱动类必须为 `org.h2.Driver`
- **THEN** 用户名必须为 `sa`，密码为空

---

### Requirement: 配置默认值策略

关键配置项必须提供合理的默认值，确保测试环境配置缺失时不会报错。

#### Scenario: context-path 默认值

- **WHEN** `server.servlet.context-path` 配置项未指定
- **THEN** 系统必须使用空字符串作为默认值
- **THEN** 应用启动不应因配置缺失而失败

#### Scenario: 日志路径默认值

- **WHEN** `logging.file.path` 配置项未指定
- **THEN** 系统必须使用 `.logs` 作为默认日志目录
- **THEN** 日志文件必须能正常写入

---

### Requirement: 测试环境配置文件位置

测试环境配置文件必须位于指定位置，确保测试代码能正确加载。

#### Scenario: 配置文件位置规范

- **WHEN** 创建测试环境配置文件
- **THEN** `application.yaml` 必须位于 `test/src/test/resources/`
- **THEN** `logback-test.xml` 必须位于 `test/src/test/resources/`
- **THEN** 配置文件必须纳入版本控制
