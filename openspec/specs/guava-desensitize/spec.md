# guava-desensitize Specification

## Purpose
TBD - created by archiving change convert-existing-specs-to-openspec. Update Purpose after archive.
## Requirements
### Requirement: 重新启用脱敏功能

The system SHALL re-enable the DesensitizingConverter desensitization functionality, resolving the async log class caching issue.

#### Scenario: 脱敏功能启用

- **WHEN** desensitization is enabled in logback-spring.xml
- **THEN** the system MUST desensitize passwords as `password=***`
- **THEN** the system MUST desensitize phone numbers as `phone=138****5678`
- **THEN** the system MUST desensitize ID numbers as `110****1234`
- **THEN** the system MUST desensitize email addresses as `user***@example.com`

#### Scenario: 高并发脱敏处理

- **WHEN** async logs handle high concurrency (100 threads)
- **THEN** desensitization MUST work correctly without class caching issues
- **THEN** desensitization processing MUST NOT throw exceptions

---

### Requirement: 使用Guava优化脱敏实现

The system SHALL use the mature Guava library to replace some custom desensitization logic to improve code maintainability and consistency.

#### Scenario: 使用Guava Strings进行字符串截断

- **WHEN** log messages exceed 2048 characters
- **THEN** Guava Strings MUST be used to truncate and append "(truncated)" suffix
- **THEN** truncation MUST work correctly

#### Scenario: 使用Guava CharMatcher进行字符过滤

- **WHEN** desensitizing bank card numbers
- **THEN** Guava CharMatcher MUST be used for character filtering
- **THEN** the result MUST preserve front and back portions with asterisks in the middle

#### Scenario: 混合使用Guava和正则表达式

- **WHEN** desensitizing sensitive information
- **THEN** performance MUST NOT be lower than original implementation
- **THEN** output format MUST be consistent with original implementation

---

### Requirement: 支持7种敏感信息类型脱敏

The system SHALL support desensitization for 7 types of sensitive information: passwords, tokens, phone numbers, ID numbers, bank card numbers, IP addresses, and email addresses.

#### Scenario: 密码脱敏

- **WHEN** logs contain passwords (e.g., "password=123456")
- **THEN** the password MUST be desensitized as "password=***"

#### Scenario: Token脱敏

- **WHEN** logs contain tokens
- **THEN** the token MUST be desensitized

#### Scenario: 手机号脱敏

- **WHEN** logs contain phone numbers (e.g., "phone=13812345678")
- **THEN** the phone number MUST be desensitized as "phone=138****5678"

#### Scenario: 身份证号脱敏

- **WHEN** logs contain ID numbers
- **THEN** the ID number MUST be desensitized as "110****1234"

#### Scenario: 银行卡号脱敏

- **WHEN** logs contain bank card numbers
- **THEN** the bank card number MUST be desensitized preserving front and back portions

#### Scenario: IP地址脱敏

- **WHEN** logs contain IP addresses
- **THEN** the IP address MUST be desensitized

#### Scenario: 邮箱脱敏

- **WHEN** logs contain email addresses (e.g., "user@example.com")
- **THEN** the email MUST be desensitized as "user***@example.com"

---

### Requirement: 保持性能要求

The system SHALL ensure desensitization functionality meets performance requirements in high-concurrency scenarios.

#### Scenario: 高并发性能测试

- **WHEN** 100 threads concurrently log 100,000 messages
- **THEN** throughput MUST be >= 3000 logs/second
- **THEN** no memory leaks or OOM MUST occur

#### Scenario: 异常情况处理

- **WHEN** exceptions occur during desensitization processing
- **THEN** exceptions MUST be caught and original log messages returned
- **THEN** log recording MUST NOT be affected

---

