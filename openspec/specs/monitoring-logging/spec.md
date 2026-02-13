# monitoring-logging Specification

## Purpose
TBD - created by archiving change migrate-spec-kit-specs-to-opencode. Update Purpose after archive.
## Requirements
### Requirement: 指标监控

The system MUST monitor key metrics to detect issues and optimize performance in a timely manner.

#### Scenario: JVM指标监控

- **WHEN** 系统运行时
- **THEN** 必须监控JVM堆内存使用率（警告阈值80%，严重阈值90%）
- **THEN** 必须监控JVM GC频率和GC时间（Full GC频率>1次/小时必须告警）
- **THEN** 必须监控线程数和线程状态（BLOCKED线程数>10必须告警）
- **THEN** 必须使用Micrometer或类似库集成JMX指标

#### Scenario: 业务指标监控

- **WHEN** 关键业务操作发生时
- **THEN** 必须记录业务指标（如订单创建数、支付成功率、API响应时间）
- **THEN** 指标必须包含维度（时间、模块、用户类型等）
- **THEN** 指标命名必须规范（如 `order_created_total`、`payment_success_rate`）
- **THEN** 业务指标必须上报到监控系统（如Prometheus + Grafana）

#### Scenario: 健康检查

- **WHEN** 客户端或监控系统查询系统健康状态
- **THEN** 必须提供健康检查端点 `/actuator/health`
- **THEN** 健康检查必须包含：数据库连接、Redis连接、消息队列连接
- **THEN** 健康检查响应时间必须<500ms（超时标记为不健康）
- **THEN** 健康状态变更时必须发送告警（从UP变为DOWN）

---

### Requirement: 链路追踪

The system MUST support distributed tracing to facilitate problem localization and performance analysis.

#### Scenario: Trace ID传递

- **WHEN** HTTP请求进入系统
- **THEN** 必须生成唯一的Trace ID（UUID格式）
- **THEN** Trace ID必须在响应头 `X-Trace-Id` 中返回给客户端
- **THEN** Trace ID必须在整个调用链中传递（通过HTTP header或MDC）
- **THEN** Trace ID必须记录到所有日志中（便于关联）

#### Scenario: Span ID记录

- **WHEN** 调用下游服务（如支付网关）
- **THEN** 必须生成Span ID（表示单个调用片段）
- **THEN** Span ID必须包含父Span ID（形成调用树）
- **THEN** Span必须记录开始时间和结束时间（计算耗时）
- **THEN** Span必须记录调用的服务和方法名

#### Scenario: 分布式追踪集成

- **WHEN** 系统使用微服务架构
- **THEN** 必须集成OpenTelemetry或Zipkin进行分布式追踪
- **THEN** 追踪数据必须上报到追踪系统（如Jaeger、SkyWalking）
- **THEN** 追踪系统必须可视化调用链（时间线、服务拓扑图）
- **THEN** 追踪数据必须保留至少7天（便于历史问题分析）

---

### Requirement: 日志分级

The system MUST use appropriate log levels to avoid excessive or insufficient logging.

#### Scenario: DEBUG级别使用

- **WHEN** 开发者需要记录详细的调试信息
- **THEN** 必须使用DEBUG级别记录详细的执行流程（如方法入参、出参、中间状态）
- **THEN** DEBUG日志仅在开发环境启用（生产环境禁止）
- **THEN** DEBUG日志必须包含足够的上下文（便于调试）
- **THEN** 禁止在循环中使用DEBUG日志记录每条数据（仅记录关键节点）

#### Scenario: INFO级别使用

- **WHEN** 记录正常的业务操作
- **THEN** 必须使用INFO级别记录关键业务事件（如订单创建、支付成功）
- **THEN** INFO日志必须包含业务上下文（订单ID、用户ID、操作时间）
- **THEN** INFO日志必须简洁（避免冗余信息）
- **THEN** INFO日志必须在所有环境启用

#### Scenario: WARN级别使用

- **WHEN** 系统遇到可忽略的异常或潜在问题
- **THEN** 必须使用WARN级别记录（如重试操作、降级处理、参数异常）
- **THEN** WARN日志必须说明忽略的原因和影响
- **THEN** WARN日志数量应该较少（频繁的WARN需要关注）
- **THEN** WARN日志必须在所有环境启用

#### Scenario: ERROR级别使用

- **WHEN** 系统发生异常或错误
- **THEN** 必须使用ERROR级别记录（如数据库连接失败、业务异常）
- **THEN** ERROR日志必须包含完整的堆栈信息
- **THEN** ERROR日志必须记录业务上下文和用户上下文
- **THEN** ERROR日志必须在所有环境启用，并且必须触发告警

---

### Requirement: 结构化日志

The system MUST use structured log formats to facilitate log retrieval and analysis.

#### Scenario: JSON格式日志

- **WHEN** 系统记录日志
- **THEN** 必须使用JSON格式（便于日志系统解析）
- **THEN** JSON结构必须包含标准字段：`timestamp`、`level`、`logger`、`message`、`thread`、`mdc`、`exception`
- **THEN** 时间戳必须是ISO 8601格式（`2026-02-12T12:34:56.789Z`）
- **THEN** 线程名必须有意义（如 `http-nio-123`，便于识别）

#### Scenario: 关键字段定义

- **WHEN** 定义结构化日志字段
- **THEN** 必须包含 `traceId`（链路追踪ID，关联所有日志）
- **THEN** 必须包含 `userId`（用户ID，关联用户操作）
- **THEN** 必须包含 `duration`（耗时ms，性能分析）
- **THEN** 必须包含 `requestId`（请求ID，关联请求和响应）

#### Scenario: MDC使用

- **WHEN** 处理HTTP请求或异步任务
- **THEN** 必须使用MDC（Mapped Diagnostic Context）存储上下文
- **THEN** MDC必须在请求开始时设置（Trace ID、User ID、Request ID）
- **THEN** MDC必须在请求结束时清理（避免线程复用污染）
- **THEN** 异步任务必须继承父线程的MDC（通过TransmittableThreadLocal）

---

### Requirement: 告警规则

The system MUST configure reasonable alerting rules to detect abnormal situations in a timely manner.

#### Scenario: 告警阈值配置

- **WHEN** 配置告警规则
- **THEN** 指标告警阈值必须基于历史数据统计（如P99、P95）
- **THEN** 告警阈值必须区分级别（INFO、WARN、ERROR、CRITICAL）
- **THEN** ERROR级别错误必须立即告警（无延迟）
- **THEN** WARN级别异常可以延迟告警（如5分钟内达到阈值才告警）

#### Scenario: 告警渠道配置

- **WHEN** 系统发送告警
- **THEN** CRITICAL级别告警必须使用多渠道（邮件、短信、钉钉、Slack）
- **THEN** ERROR级别告警必须发送到邮件和即时通讯工具
- **THEN** WARN级别告警可以仅发送到日志系统（减少噪音）
- **THEN** 告警必须包含关键信息（告警时间、指标值、阈值、影响范围）

#### Scenario: 告警收敛和降噪

- **WHEN** 相同告警短时间内重复发生
- **THEN** 必须实施告警收敛（5分钟内相同告警仅发送1次）
- **THEN** 告警必须包含累计次数（如 `订单创建失败已发生10次`）
- **THEN** 告警收敛时必须标注已收敛（避免重复通知）
- **THEN** 夜间告警（23:00-07:00）可以降级为邮件（非即时通讯）

---

