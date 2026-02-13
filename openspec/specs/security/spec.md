# security Specification

## Purpose
TBD - created by archiving change migrate-spec-kit-specs-to-opencode. Update Purpose after archive.
## Requirements
### Requirement: SQL注入防护

The system SHALL prevent all SQL injection attacks, using parameterized queries or secure mechanisms of ORM frameworks.

#### Scenario: 使用MyBatis-Flex进行参数化查询

- **WHEN** 开发者使用MyBatis-Flex进行数据库操作
- **THEN** 系统必须使用#{}或${}占位符进行参数绑定，禁止字符串拼接
- **THEN** MyBatis-Flex Mapper接口方法必须使用@Param注解或自动参数映射
- **THEN** 禁止使用 `${}` 直接注入原始SQL（动态表名等特殊场景除外，需额外代码审查）

#### Scenario: 使用JPA/Hibernate进行参数化查询

- **WHEN** 开发者使用JPA或Hibernate进行数据库操作
- **THEN** 必须使用Criteria API、JPQL参数绑定或原生SQL参数化
- **THEN** 禁止字符串拼接SQL语句
- **THEN** 动态查询必须使用JPA Criteria API或Specification模式

#### Scenario: 批量操作的安全处理

- **WHEN** 开发者执行批量插入或批量更新操作
- **THEN** 必须使用MyBatis-Flex的批量操作方法（insertBatch、updateBatch）
- **THEN** 禁止在循环中拼接SQL语句
- **THEN** 批量操作的数据量必须有上限（默认1000条），超过上限必须分批处理

---

### Requirement: XSS攻击防护

The system SHALL prevent all cross-site scripting (XSS) attacks, implementing protection at both input validation and output encoding levels.

#### Scenario: 用户输入验证

- **WHEN** 接收用户输入（表单、API参数、文件上传等）
- **THEN** 必须对输入进行白名单验证或黑名单过滤
- **THEN** 对于富文本输入，必须使用JSoup或类似库进行HTML清理
- **THEN** 对于URL输入，必须验证协议和域名白名单
- **THEN** 输入长度必须在合理范围内（根据业务限制）

#### Scenario: 输出编码

- **WHEN** 将用户输入的内容输出到HTML页面
- **THEN** 必须对特殊字符进行HTML实体编码（<, >, ", ', &等）
- **THEN** 使用模板引擎（如Thymeleaf）的自动转义机制
- **THEN** 禁止直接使用 `innerHTML` 或类似不安全的方法插入用户内容

#### Scenario: Content Security Policy (CSP)

- **WHEN** 系统响应HTML页面
- **THEN** 必须设置Content-Security-Policy HTTP头
- **THEN** CSP策略必须限制script-src、style-src、img-src等资源来源
- **THEN** 禁止使用 `unsafe-inline` 或 `unsafe-eval`（除非必要且有额外保护）

---

### Requirement: CSRF攻击防护

The system SHALL prevent cross-site request forgery (CSRF) attacks, using token verification or SameSite cookies.

#### Scenario: 使用CSRF Token进行防护

- **WHEN** 用户执行状态改变操作（POST、PUT、DELETE）
- **THEN** 系统必须要求CSRF Token
- **THEN** Token必须在Session中生成，每个请求验证后更新
- **THEN** GET请求不要求CSRF Token（仅用于读取操作）
- **THEN** API响应必须包含新的CSRF Token（用于下一个请求）

#### Scenario: 使用SameSite Cookie进行防护

- **WHEN** 系统使用Cookie进行Session管理
- **THEN** 必须设置SameSite属性为 `Strict` 或 `Lax`
- **THEN** 对于跨站请求（如OAuth回调），必须使用 `Lax`
- **THEN** 对于所有内部操作，必须使用 `Strict`
- **THEN** 必须设置Cookie的 `Secure` 和 `HttpOnly` 属性

---

### Requirement: 认证授权规范

The system SHALL provide secure authentication and authorization mechanisms to protect access to sensitive resources.

#### Scenario: JWT Token安全配置

- **WHEN** 系统使用JWT进行用户认证
- **THEN** JWT必须使用强加密算法（HS256或RS256）
- **THEN** JWT的exp（过期时间）必须在合理范围内（默认15分钟，最长2小时）
- **THEN** JWT必须在HTTPS环境下传输（通过nginx反向代理强制HTTPS）
- **THEN** Refresh Token必须有过期时间，且比Access Token长（默认7天）
- **THEN** Refresh Token必须存储在HttpOnly Cookie中，不在URL参数中传递

#### Scenario: Session安全配置

- **WHEN** 系统使用Session进行用户认证
- **THEN** Session必须固定（防止Session Fixation攻击）
- **THEN** Session必须有超时时间（默认30分钟无活动自动失效）
- **THEN** Session ID必须在登录时重新生成
- **THEN** 用户登出时必须使Session失效
- **THEN** Session必须存储在服务器端，客户端仅持有Session ID

#### Scenario: 密码安全存储

- **WHEN** 系统存储用户密码
- **THEN** 必须使用BCrypt、Argon2或PBKDF2等安全哈希算法
- **THEN** 哈希算法的工作因子（work factor）必须符合安全建议（BCrypt cost≥10）
- **THEN** 禁止存储明文密码或MD5、SHA1等弱哈希
- **THEN** 密码必须有复杂度要求（至少8位，包含大小写字母、数字、特殊字符）

#### Scenario: 权限控制

- **WHEN** 用户访问受保护的资源
- **THEN** 系统必须验证用户权限
- **THEN** 权限检查必须在应用层（ApplicationService）进行
- **THEN** 禁止在Controller层进行权限判断（必须在Service层统一）
- **THEN** 权限模型必须遵循最小权限原则

---

### Requirement: 敏感数据处理

The system SHALL properly handle sensitive data (passwords, phone numbers, ID numbers, bank card numbers, etc.) to prevent leakage.

#### Scenario: 敏感数据加密存储

- **WHEN** 系统存储敏感数据（密码、身份证号、银行卡号等）
- **THEN** 必须使用强加密算法（AES-256）
- **THEN** 加密密钥必须使用KMS（密钥管理服务）或环境变量管理
- **THEN** 加密密钥禁止硬编码在代码中
- **THEN** 数据库中必须使用binary类型存储加密数据（禁止明文字段）

#### Scenario: 敏感数据传输加密

- **WHEN** 敏感数据通过网络传输
- **THEN** 必须使用TLS 1.2或更高版本加密
- **THEN** 禁止使用HTTP传输敏感数据
- **THEN** 必须强制HTTPS（通过nginx配置301重定向）
- **THEN** 必须禁用弱加密算法（SSLv2、SSLv3、TLS 1.0、TLS 1.1）

#### Scenario: 日志脱敏

- **WHEN** 系统记录包含敏感信息的日志
- **THEN** 必须对敏感字段进行脱敏处理（密码脱敏为`******`）
- **THEN** 手机号脱敏格式：`138****1234`
- **THEN** 身份证号脱敏格式：`110***********`（仅保留前3位）
- **THEN** 银行卡号脱敏格式：`6222***********`（仅保留前4位）

---

### Requirement: 依赖安全

The system SHALL manage security vulnerabilities in third-party dependencies and update dependencies regularly.

#### Scenario: 依赖漏洞扫描

- **WHEN** 项目进行CI/CD构建
- **THEN** 必须执行依赖漏洞扫描（使用OWASP Dependency Check或Snyk）
- **THEN** 扫描结果必须包含在构建报告中
- **THEN** 如果发现高危漏洞（CVSS≥7.0），必须阻断构建
- **THEN** 扫描失败必须通知安全团队

#### Scenario: 依赖版本管理

- **WHEN** 引入新的第三方依赖
- **THEN** 必须选择最新稳定版本（避免使用alpha/beta版本）
- **THEN** 必须定期检查依赖的安全公告（如Log4j、Fastjson历史漏洞）
- **THEN** 已知有漏洞的依赖必须在24小时内更新到安全版本
- **THEN** 依赖更新必须包含回归测试

#### Scenario: 禁止使用不安全的依赖

- **WHEN** 开发者选择第三方库
- **THEN** 禁止使用已知有严重漏洞的库（如Fastjson、旧版Log4j）
- **THEN** 必须优先使用维护活跃的库（避免使用长期未更新的库）
- **THEN** 禁止使用不安全的加密实现（如DES、MD5、SHA1）

---

