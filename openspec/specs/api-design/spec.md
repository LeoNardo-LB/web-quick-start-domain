# api-design Specification

## Purpose
TBD - created by archiving change migrate-spec-kit-specs-to-opencode. Update Purpose after archive.
## Requirements
### Requirement: API命名规范

The system SHALL follow RESTful naming conventions to ensure API readability and consistency.

#### Scenario: RESTful路径命名

- **WHEN** 开发者定义REST API路径
- **THEN** 必须使用名词复数形式（如 `/api/orders`、`/api/users`）
- **THEN** 禁止在URL中包含动词（如 `/api/createOrder`）
- **THEN** 资源层级关系使用嵌套路径（如 `/api/orders/123/items`）
- **THEN** 参数使用path variable而非query string（如 `/api/orders/123` 而非 `/api/orders?id=123`）

#### Scenario: HTTP动词选择

- **WHEN** 开发者选择HTTP方法
- **THEN** 必须使用标准动词：GET（读取）、POST（创建）、PUT（更新）、DELETE（删除）
- **THEN** PATCH方法仅用于部分更新（非必须时避免使用）
- **THEN** 对于复杂查询，使用GET + query parameters而非POST body
- **THEN** POST用于创建资源时，必须返回201状态码和Location头

#### Scenario: 控制器命名规范

- **WHEN** 开发者创建Controller类
- **THEN** 类名必须以资源名+Controller结尾（如 `OrderController`、`UserController`）
- **THEN** 必须使用 `@RestController` 注解
- **THEN** 类级别必须使用 `@RequestMapping("/api/{resource}")` 定义基础路径
- **THEN** 方法级别使用 `@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping` 注解

---

### Requirement: 版本控制策略

The system SHALL provide API version control mechanisms to ensure backward compatibility.

#### Scenario: URI版本控制

- **WHEN** 需要进行API版本控制
- **THEN** 必须使用 `/api/v{version}/{resource}` 格式（如 `/api/v1/orders`）
- **THEN** 版本号必须使用语义化版本（Semantic Versioning，如 v1、v2）
- **THEN** 禁止在URL中包含小版本号（如 v1.1.2），仅主版本号
- **THEN** 新版本发布时，旧版本必须保留至少3个月（渐进式迁移）

#### Scenario: Header版本控制

- **WHEN** 需要进行API版本控制
- **THEN** 必须使用 `Accept` 或 `X-API-Version` header指定版本
- **THEN** 版本格式为 `application/vnd.api.v1+json` 或 `v1`
- **THEN** 未指定版本时，必须默认使用最新稳定版本（非beta版本）
- **THEN** 版本不匹配时，必须返回406 Not Acceptable错误

#### Scenario: 版本废弃通知

- **WHEN** 某个API版本被废弃
- **THEN** 必须在响应头中包含 `X-API-Deprecated: true` 和 `X-API-Deprecated-Date`
- **THEN** 响应体中必须包含 `deprecation` 字段，说明废弃原因和建议替代版本
- **THEN** 日志中必须记录废弃版本的调用次数，监控迁移进度

---

### Requirement: 分页/排序/过滤标准参数

The system SHALL provide unified pagination, sorting, and filtering parameters to enhance API flexibility.

#### Scenario: 统一分页参数

- **WHEN** 开发者实现分页查询
- **THEN** 必须使用标准参数：`page`（当前页码，从0开始）、`size`（每页大小）
- **THEN** 每页大小必须有上限（默认100），超过上限时使用最大值
- **THEN** 必须返回分页元数据（total、totalPages、currentPage、hasNext、hasPrevious）
- **THEN** 禁止使用offset/limit参数（page/size更符合用户习惯）

#### Scenario: 统一排序参数

- **WHEN** 开发者实现排序功能
- **THEN** 必须使用 `sort` 参数（支持逗号分隔的多字段排序）
- **THEN** 排序方向使用 `:asc` 或 `:desc` 后缀（如 `sort=createdAt:desc,amount:asc`）
- **THEN** 支持排序的字段必须在API文档中明确列出
- **THEN** 未指定排序时，必须使用默认排序（如创建时间倒序）

#### Scenario: 统一过滤参数

- **WHEN** 开发者实现过滤功能
- **THEN** 必须使用参数名对应字段名（如 `status`、`startDate`、`endDate`）
- **THEN** 支持范围查询（如 `amountMin`、`amountMax`）
- **THEN** 支持模糊查询（如 `name=keyword` 使用LIKE查询）
- **THEN** 复杂查询支持Q语言或DSL（禁止在query parameter中传递复杂对象）

---

### Requirement: 响应格式规范

The system SHALL return unified response formats to facilitate client processing.

#### Scenario: 成功响应格式

- **WHEN** API请求成功
- **THEN** 必须返回统一的包装格式：`{ success: true, data: {...}, message: "操作成功", timestamp: 1234567890 }`
- **THEN** `data` 字段必须包含实际的业务数据（订单、用户等）
- **THEN** `timestamp` 必须是Unix时间戳（毫秒级）
- **THEN** GET查询（列表）和GET查询（详情）使用相同的响应格式

#### Scenario: 错误响应格式

- **WHEN** API请求失败
- **THEN** 必须返回统一的错误格式：`{ success: false, errorCode: "ORDER_NOT_FOUND", errorMessage: "订单不存在", timestamp: 1234567890 }`
- **THEN** `errorCode` 必须是常量（枚举或常量类，禁止硬编码）
- **THEN** `errorMessage` 必须支持国际化（根据Accept-Language header返回对应语言）
- **THEN** 生产环境中，`errorMessage` 禁止包含技术堆栈（仅在开发环境返回）

#### Scenario: 分页响应格式

- **WHEN** API返回分页数据
- **THEN** `data` 字段必须包含：`{ items: [...], total: 100, totalPages: 10, currentPage: 1, hasNext: true, hasPrevious: false }`
- **THEN** `items` 必须是数组，单条查询也使用数组
- **THEN** `total` 表示总记录数（用于前端显示"共X条"）
- **THEN** 禁止返回数据库的page和pageSize（前端无需知道后端分页逻辑）

---

### Requirement: 幂等性设计

The system SHALL ensure idempotency of critical operations to prevent side effects from duplicate requests.

#### Scenario: POST幂等性

- **WHEN** 创建资源时需要幂等性（如支付订单）
- **THEN** 必须要求客户端提供 `Idempotency-Key` header或请求参数
- **THEN** 第一次处理时，必须将Idempotency-Key与结果关联并存储（有效期24小时）
- **THEN** 重复请求时，必须返回已缓存的结果（不重复处理）
- **THEN** 响应头中必须包含 `X-Idempotency-Key` 标识是否使用了幂等键

#### Scenario: PUT幂等性

- **WHEN** 更新资源时
- **THEN** 必须使用PUT方法（幂等性保证）
- **THEN** 更新操作必须是全量替换（禁止部分更新）
- **THEN** 多次相同PUT请求必须产生相同结果（资源状态一致）
- **THEN** 禁止在PUT中修改资源的ID（ID在URL中，不在body中）

#### Scenario: DELETE幂等性

- **WHEN** 删除资源时
- **THEN** 必须使用DELETE方法
- **THEN** 多次删除相同ID的资源，第一次返回200，后续返回404（已删除）
- **THEN** 禁止删除时返回资源信息（仅返回成功状态）

---

### Requirement: 限流与降级

The system SHALL implement rate limiting and circuit breaker mechanisms to protect system stability.

#### Scenario: 接口级限流

- **WHEN** 开发者配置接口限流
- **THEN** 必须使用注解 `@RateLimiter(value = "order:create", limit = 100, period = "1m")`
- **THEN** 限流基于用户IP、用户ID、接口维度（根据业务选择）
- **THEN** 超过限流阈值时，必须返回429 Too Many Requests错误
- **THEN** 响应头中必须包含 `X-RateLimit-Limit`、`X-RateLimit-Remaining`、`X-RateLimit-Reset`

#### Scenario: 熔断器配置

- **WHEN** 调用外部服务（如支付网关）时
- **THEN** 必须配置熔断器（如Resilience4j的@CircuitBreaker）
- **THEN** 熔断阈值必须合理设置（如10次/10秒失败率>50%）
- **THEN** 熔断打开时，必须快速失败（fallback）而非等待超时
- **THEN** 熔断状态变更时，必须发送告警通知

#### Scenario: 降级策略

- **WHEN** 外部服务不可用或响应超时
- **THEN** 必须执行降级策略（返回默认值或缓存数据）
- **THEN** 降级必须记录日志（降级原因、降级时间）
- **THEN** 禁止降级时返回敏感数据（如用户余额）
- **THEN** 降级数据必须有标记（如 `{ data: {...}, degraded: true, reason: "支付服务超时" }`）

---

