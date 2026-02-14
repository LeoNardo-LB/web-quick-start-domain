## ADDED Requirements

### Requirement: Bean Validation 异常处理

系统必须正确处理 Bean Validation 框架抛出的参数校验异常。

#### Scenario: MethodArgumentNotValidException 处理

- **WHEN** Controller 方法参数使用 `@Valid` 注解且校验失败
- **THEN** 系统必须捕获 `MethodArgumentNotValidException` 异常
- **THEN** HTTP 响应状态码必须为 `400 Bad Request`
- **THEN** 响应体必须包含具体的校验失败字段和错误消息

#### Scenario: BindException 处理

- **WHEN** 表单绑定失败（如类型转换错误）
- **THEN** 系统必须捕获 `BindException` 异常
- **THEN** HTTP 响应状态码必须为 `400 Bad Request`
- **THEN** 响应体必须包含绑定失败的字段信息

#### Scenario: ConstraintViolationException 处理

- **WHEN** 使用 `@Validated` 注解的服务方法参数校验失败
- **THEN** 系统必须捕获 `ConstraintViolationException` 异常
- **THEN** HTTP 响应状态码必须为 `400 Bad Request`
- **THEN** 响应体必须包含约束违反的详细信息

---

### Requirement: 参数校验异常消息格式

参数校验异常必须返回结构化的错误信息，便于客户端展示。

#### Scenario: 字段级错误信息

- **WHEN** 参数校验失败
- **THEN** 响应体必须包含 `field` 字段（失败的字段名）
- **THEN** 响应体必须包含 `message` 字段（校验失败原因）
- **THEN** 对于嵌套对象，字段名必须使用点分隔（如 `address.city`）

#### Scenario: 多字段校验失败

- **WHEN** 多个字段同时校验失败
- **THEN** 响应体必须包含所有失败字段的错误信息
- **THEN** 错误信息必须按字段名排序
- **THEN** 响应体格式必须为 `{ success: false, code: 400, message: "参数校验失败", errors: [...] }`
