# error-code-design Specification

## Purpose
TBD - created by archiving change migrate-spec-kit-specs-to-opencode. Update Purpose after archive.
## Requirements
### Requirement: 错误码体系设计

The system SHALL establish a unified error code system to facilitate error tracking and client-side handling.

#### Scenario: 错误码分类

- **WHEN** 设计错误码体系
- **THEN** 错误码必须按模块分类（格式：`{MODULE}_{ERROR_CODE}`）
- **THEN** 模块编码必须为3位大写字母（如 `ORD`-订单模块、`USR`-用户模块、`SYS`-系统模块）
- **THEN** 错误码必须为4位数字（如 `0001`、`0002`）
- **THEN** 完整错误码示例：`ORD_0001`（订单不存在）、`USR_0001`（用户不存在）

#### Scenario: 错误码编码规则

- **WHEN** 分配错误码编号
- **THEN** 必须预留区间（如 `0001-0999`为通用错误、`1001-1999`为业务错误、`2001-2999`为系统错误）
- **THEN** 每个模块的错误码必须有唯一编号，禁止重复
- **THEN** 错误码必须包含在枚举或常量类中（禁止硬编码）
- **THEN** 错误码必须有序（按逻辑顺序编号，便于维护）

#### Scenario: 错误码扩展性设计

- **WHEN** 需要新增错误码
- **THEN** 必须在预留区间内分配编号
- **THEN** 新增错误码必须更新文档（错误码清单）
- **THEN** 禁止删除已废弃的错误码（标记为deprecated，保留至少3个版本）
- **THEN** 错误码变更必须在变更日志中记录

---

### Requirement: 国际化错误信息

The system SHALL support multilingual error messages to accommodate users in different regions.

#### Scenario: 错误信息i18n支持

- **WHEN** 系统返回错误信息
- **THEN** 必须根据Accept-Language header返回对应语言
- **THEN** 支持的语言必须包括：`zh-CN`（简体中文）、`en-US`（美式英语）、`ja-JP`（日语）
- **THEN** 未指定语言时，必须默认使用 `zh-CN`
- **THEN** 错误消息文件必须按语言分文件存储（如 `messages_zh_CN.properties`、`messages_en_US.properties`）

#### Scenario: 错误消息格式

- **WHEN** 编写错误消息
- **THEN** 必须使用简洁明了的语言（禁止技术术语）
- **THEN** 必须包含错误原因和建议操作（如 `订单不存在，请检查订单编号后重试`）
- **THEN** 禁止返回堆栈信息（生产环境）
- **THEN** 禁止使用 `null`、`undefined` 等无意义词

#### Scenario: 错误消息参数化

- **WHEN** 错误信息包含动态内容（如订单ID）
- **THEN** 必须使用参数化消息（如 `订单 {0} 不存在`）
- **THEN** 参数占位符使用 `{0}`、`{1}` 格式（Spring MessageSource标准）
- **THEN** 参数必须按顺序提供，避免混淆
- **THEN** 禁止使用字符串拼接（`订单 ` + orderId + ` 不存在`）

---

### Requirement: 错误日志记录规范

The system SHALL record error logs uniformly to facilitate problem tracking and analysis.

#### Scenario: 错误日志格式

- **WHEN** 记录错误日志
- **THEN** 必须使用统一格式：`[ERROR] 类#方法 | 错误描述 | 耗时ms | 线程 | 错误码 | 错误信息 | TraceId`
- **THEN** TraceId必须包含在日志中（从MDC或ThreadLocal获取）
- **THEN** 错误堆栈必须记录（缩进格式，便于阅读）
- **THEN** 关键业务上下文必须记录（如订单ID、用户ID、请求参数）
- **THEN** 错误类型必须明确（业务异常、系统异常、参数异常）

#### Scenario: 错误日志分级

- **WHEN** 系统发生异常
- **THEN** 必须使用ERROR级别记录
- **THEN** 业务异常（如订单状态不符）使用ERROR级别
- **THEN** 系统异常（如数据库连接失败）使用ERROR级别
- **THEN** 参数异常（如必填字段缺失）使用WARN级别
- **THEN** 禁止使用INFO级别记录异常

#### Scenario: 错误日志上下文

- **WHEN** 记录错误日志
- **THEN** 必须记录请求上下文（URL、Method、Headers、Body）
- **THEN** 敏感字段必须脱敏（密码、身份证号、银行卡号）
- **THEN** 必须记录用户上下文（UserId、UserName、IpAddress）
- **THEN** 对于异步错误，必须记录触发源（如 `Event: OrderPaidEvent`）

---

### Requirement: 客户端友好错误

The system SHALL return error information that is easy for clients to handle to improve user experience.

#### Scenario: 错误消息清晰度

- **WHEN** 返回错误信息
- **THEN** 必须使用用户语言（非技术术语）
- **THEN** 必须说明问题的原因（如 `账户余额不足`）
- **THEN** 必须提供解决方案建议（如 `请充值后重试`）
- **THEN** 禁止返回模糊信息（如 `操作失败`，必须说明具体原因）

#### Scenario: HTTP状态码使用

- **WHEN** API请求失败
- **THEN** 必须返回正确的HTTP状态码
- **THEN** 客户端错误（参数缺失、格式错误）返回400 Bad Request
- **THEN** 认证失败返回401 Unauthorized
- **THEN** 权限不足返回403 Forbidden
- **THEN** 资源不存在返回404 Not Found
- **THEN** 业务错误（如库存不足）返回422 Unprocessable Entity

#### Scenario: 错误码客户端展示

- **WHEN** 前端展示错误信息
- **THEN** 必须根据errorCode从错误消息映射表获取显示文本
- **THEN** 前端禁止直接显示后端的errorMessage（需要通过errorCode映射）
- **THEN** 前端必须支持错误信息的样式（颜色、图标）
- **THEN** 前端必须提供"重新尝试"、"联系客服"等操作按钮

---

### Requirement: 服务端调试错误

The system SHALL provide detailed debugging information in development/test environments to facilitate problem localization.

#### Scenario: 开发环境错误详情

- **WHEN** 系统运行在开发环境（`spring.profiles.active=dev`）
- **THEN** 错误响应必须包含详细堆栈信息
- **THEN** 必须包含内部状态（如当前订单状态、用户余额）
- **THEN** 必须包含SQL语句（对于数据库错误）
- **THEN** 错误响应格式：`{ success: false, errorCode: "DB_ERROR", errorMessage: "...", stackTrace: "...", debugInfo: {...} }`

#### Scenario: 测试环境错误详情

- **WHEN** 系统运行在测试环境（`spring.profiles.active=test`）
- **THEN** 错误响应必须包含部分调试信息（如堆栈，但不包含SQL）
- **THEN** 必须记录Trace ID，便于关联日志
- **THEN** 生产敏感数据可以脱敏（如手机号脱敏）
- **THEN** 错误响应格式与开发环境类似，但debugInfo字段简化

#### Scenario: 生产环境错误简化

- **WHEN** 系统运行在生产环境（`spring.profiles.active=prod`）
- **THEN** 错误响应必须移除所有调试信息（stackTrace、debugInfo）
- **THEN** 仅返回errorCode和errorMessage
- **THEN** 禁止返回内部状态或SQL语句
- **THEN** 详细的错误信息仅记录到日志（便于运维查看，不暴露给客户端）

---

