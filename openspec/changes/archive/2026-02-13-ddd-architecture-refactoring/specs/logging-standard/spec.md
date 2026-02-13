# Logging Standard Specification

## ADDED Requirements

### Requirement: Unified log format

All log statements SHALL follow the unified format:

```
[类型] 类#方法 | 业务描述 | 耗时ms | 线程 | 入参 | 出参/错误
```

#### Scenario: Log format components

- **WHEN** logging any message
- **THEN** the log MUST include:
  - **类型**: Business category (ORDER, USER, PAYMENT, etc.)
  - **类#方法**: Automatically added by LogAspect
  - **业务描述**: Human-readable description
  - **耗时ms**: Automatically added by LogAspect (for method execution)
  - **线程**: Automatically added by LogAspect
  - **入参**: Method input parameters
  - **出参/错误**: Return value or exception

#### Scenario: Business log example

- **WHEN** logging a successful order creation
- **THEN** the log format MUST be:
  ```
  [ORDER] OrderAppService#createOrder | 创建订单成功 | 45ms | virtual-1 | customerId=C001 | orderId=O123
  ```

### Requirement: Log level usage

Log levels SHALL be used consistently according to their purpose.

#### Scenario: DEBUG level usage

- **WHEN** logging detailed logic flow for debugging
- **THEN** DEBUG level MUST be used
- **AND** these logs MAY be disabled in production

#### Scenario: INFO level usage

- **WHEN** logging business operations and state changes
- **THEN** INFO level MUST be used
- **AND** these logs MUST be enabled in production

#### Scenario: WARN level usage

- **WHEN** logging recoverable issues or boundary conditions
- **THEN** WARN level MUST be used
- **AND** these logs MUST be enabled in production

#### Scenario: ERROR level usage

- **WHEN** logging exceptions or system errors
- **THEN** ERROR level MUST be used
- **AND** full stack trace MUST be included

### Requirement: LogAspect automatic metadata

LogAspect SHALL automatically add metadata to log statements.

#### Scenario: Method execution logging

- **WHEN** a method annotated with `@MyLog` (or similar) is executed
- **THEN** LogAspect MUST automatically add:
  - Class name and method name
  - Execution time in milliseconds
  - Thread name
  - Trace ID (if available)

#### Scenario: Controller method logging

- **WHEN** a Controller method is executed
- **THEN** request parameters and response MUST be logged
- **AND** sensitive data MUST be desensitized

### Requirement: Sensitive data desensitization

Sensitive data in logs SHALL be automatically desensitized.

#### Scenario: Password desensitization

- **WHEN** logging data containing password fields
- **THEN** the password MUST be replaced with `******`
- **AND** the original value MUST NOT appear in logs

#### Scenario: Phone number desensitization

- **WHEN** logging phone numbers
- **THEN** the format MUST be `138****1234`
- **AND** middle 4 digits MUST be masked

#### Scenario: ID card desensitization

- **WHEN** logging ID card numbers
- **THEN** the format MUST be `310***********1234`
- **AND** only first 3 and last 4 digits are visible

### Requirement: No Chinese-English mixed logs

Log messages SHALL use consistent language (Chinese preferred for this project).

#### Scenario: Chinese log messages

- **WHEN** writing log messages
- **THEN** Chinese MUST be used for business descriptions
- **AND** technical terms MAY remain in English

#### Scenario: Consistent message format

- **WHEN** logging similar operations
- **THEN** the message format MUST be consistent
- **AND** use the same terminology

### Requirement: Trace ID propagation

All logs within a request context SHALL include the same trace ID.

#### Scenario: Trace ID in BaseResult

- **WHEN** returning API response
- **THEN** `traceId` field MUST be populated
- **AND** NOT be hardcoded as "TODO"

#### Scenario: Trace ID propagation

- **WHEN** processing a request across multiple services
- **THEN** all log entries MUST include the same trace ID
- **AND** the trace ID MUST be passed to external service calls
