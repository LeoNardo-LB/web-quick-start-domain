## 0. 前置准备

- [x] 0.1 阅读 AGENTS.md 了解技能加载要求
- [x] 0.2 使用 `/tdd-workflow` 命令加载 TDD 流程

## 1. 测试环境日志配置

- [x] 1.1 规范化 `test/src/test/resources/logback-test.xml` 配置文件
    - 确保包含所有必需 appender（CONSOLE、FILE、ASYNC_FILE、ASYNC_CURRENT、ERROR_FILE、AUDIT_FILE）
    - 配置敏感信息脱敏转换器（DesensitizingConverter）
    - 移除对 Spring Profile 的依赖
- [x] 1.2 验证 `LoggingComplianceUTest` 所有测试通过
    - `testAuditLogIsolation` - 验证 AUDIT_FILE appender 存在
    - `testAuditLogRetentionPeriod` - 验证审计日志保留期 180 天
    - `testAsyncQueueSize` - 验证 ASYNC_FILE 和 ASYNC_CURRENT appender 存在

## 2. 测试环境数据库配置

- [x] 2.1 规范化 `test/src/test/resources/application.yaml` H2 配置
    - JDBC URL: `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE`
    - 驱动类: `org.h2.Driver`
    - 用户名: `sa`，密码为空
- [x] 2.2 验证所有集成测试通过
    - 运行 `mvn test -pl test` 验证 100% 通过率

## 3. 异常处理增强

- [x] 3.1 在 `WebExceptionAdvise` 添加 `MethodArgumentNotValidException` 处理器
    - 返回 HTTP 400 Bad Request
    - 返回结构化错误信息（field、message）
    - 支持多字段校验失败
- [x] 3.2 添加 `BindException` 处理器
    - 返回 HTTP 400 Bad Request
    - 返回绑定失败的详细信息
- [x] 3.3 添加 `ConstraintViolationException` 处理器
    - 返回 HTTP 400 Bad Request
    - 返回约束违反的详细信息
- [x] 3.4 编写异常处理器单元测试
    - 测试 `MethodArgumentNotValidException` 处理
    - 测试 `BindException` 处理
    - 测试 `ConstraintViolationException` 处理

## 4. 技术债务清理

- [x] 4.1 清理 `LoggingComplianceUTest` 中的过时注释
    - 移除"infrastructure 模块编译错误导致 Spring 上下文无法加载"的误导性注释
    - 更新测试类的 Javadoc 说明
- [x] 4.2 检查其他测试类中是否存在过时的 `@Disabled` 注解
    - 搜索并移除所有过时的 `@Disabled` 注解
    - 验证移除后测试能正常通过

## 5. 配置默认值

- [x] 5.1 在 `ApplicationBootstrap.java` 中为 `context-path` 添加默认值
    - 使用 `@Value("${server.servlet.context-path:}")` 提供空字符串默认值
- [x] 5.2 验证配置默认值生效
    - 测试无 `context-path` 配置时应用正常启动

## 6. 验证与文档

- [x] 6.1 运行完整测试套件验证
    - 执行 `mvn test -pl test` 确保 151 个测试全部通过
    - 执行 `mvn verify -pl test` 生成覆盖率报告
- [x] 6.2 更新相关文档
    - 更新 `test/AGENTS.md` 添加测试环境配置说明
    - 更新 `README.md` 的测试部分说明
