# fix-logging-output Specification

## Purpose
TBD - created by archiving change convert-existing-specs-to-openspec. Update Purpose after archive.
## Requirements
### Requirement: 修复日志输出路径问题

The system SHALL output log files to the `.logs` folder inside the project root directory, not to a folder at the same level as the project root.

#### Scenario: 日志输出到项目内部

- **WHEN** the application runs and produces logs
- **THEN** log files MUST be output to the `.logs` folder under the project root directory (e.g., `web-quick-start-domain/.logs/`)
- **THEN** log files MUST NOT be output to folders outside the project

#### Scenario: 日志路径不受工作目录影响

- **WHEN** the project is started from different working directories (IDE or command line)
- **THEN** log files MUST always be output to a fixed location inside the project
- **THEN** the log path MUST NOT be affected by the working directory

---

### Requirement: 优化日志配置以提升性能

The system SHALL optimize log configuration to improve log recording performance while ensuring log formats comply with project standards.

#### Scenario: 异步日志队列优化

- **WHEN** the application handles high-concurrency requests
- **THEN** the async log queue size MUST be increased to at least 2048
- **THEN** log recording performance MUST NOT be lower than before optimization
- **THEN** the log format MUST comply with `[类型] 类#方法 | 业务描述 | 耗时ms | 线程 | 入参 | 出参/错误`

#### Scenario: 日志格式统一

- **WHEN** the application runs and produces logs
- **THEN** all log output formats MUST be unified
- **THEN** the log format MUST facilitate log analysis and problem investigation

---

### Requirement: 环境差异化配置

The system SHALL differentiate log configurations for different environments using Spring Profile.

#### Scenario: 开发环境配置

- **WHEN** the application runs with dev profile
- **THEN** the root log level MUST be set to DEBUG
- **THEN** domain/app log level MUST be set to TRACE
- **THEN** performance monitoring alerts MUST be disabled

#### Scenario: 生产环境配置

- **WHEN** the application runs with production profile
- **THEN** the root log level MUST be set to INFO
- **THEN** domain/app log level MUST be set to WARN
- **THEN** error rate monitoring (threshold >1/min) and log volume anomaly detection MUST be enabled

---

### Requirement: 敏感信息脱敏和审计日志

The system SHALL desensitize sensitive information in logs and maintain audit logs for compliance requirements.

#### Scenario: 敏感信息脱敏

- **WHEN** logs contain sensitive information (passwords, tokens, phone numbers, ID numbers, bank card numbers, IP addresses)
- **THEN** the sensitive information MUST be desensitized with standard marker `***`
- **THEN** desensization coverage MUST reach 100%

#### Scenario: 审计日志

- **WHEN** sensitive operations occur (user login, permission changes, data deletion)
- **THEN** the system MUST record audit logs in `audit.log`
- **THEN** audit logs MUST be retained for at least 180 days
- **THEN** audit logs MUST support fast retrieval (<1 second)

---

