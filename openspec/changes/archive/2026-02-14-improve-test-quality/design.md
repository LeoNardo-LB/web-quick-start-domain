## Context

### 背景

测试失败分析揭示了三个核心问题：

1. **测试环境配置耦合**: `logback-spring.xml` 使用 Spring Profile，单元测试不启动 Spring 导致 appender 未加载；测试环境无 MySQL 但配置了 MySQL
   数据源
2. **异常处理不完整**: `WebExceptionAdvise` 未覆盖 `MethodArgumentNotValidException`，导致 Bean Validation 异常返回 HTTP 500
3. **技术债务**: `LoggingComplianceUTest` 中的 `@Disabled` 注释和"编译错误"注释是过时的调试遗留

### 当前状态

- 测试通过率: 100% (修复后)
- 存在 `logback-test.xml` 文件（临时创建）
- `WebExceptionAdvise` 已添加 `MethodArgumentNotValidException` 处理器（临时修复）
- 技术债务未清理

### 约束

- 遵循 DDD 四层架构原则
- 配置类必须在 `start` 模块
- 测试代码必须在独立的 `test` 模块
- 不引入新的外部依赖

## Goals / Non-Goals

**Goals:**

- 建立测试环境配置规范，确保测试与生产环境解耦
- 标准化异常处理机制，确保 Bean Validation 异常返回正确的 HTTP 状态码
- 清理技术债务，移除过时的 `@Disabled` 和误导性注释
- 将临时修复转化为正式规范

**Non-Goals:**

- 不重构 Repository 实现（`OrderRepositoryImpl` 的存根问题）
- 不修改业务逻辑
- 不引入新的测试框架或工具

## Decisions

### Decision 1: 测试专用 Logback 配置

**选择**: 创建 `logback-test.xml` 作为测试专用配置

**原因**:

- Logback 在测试环境会自动加载 `logback-test.xml`，优先级高于 `logback-spring.xml`
- 避免单元测试依赖 Spring Profile 机制
- 测试配置与生产配置完全解耦

**备选方案**:

- 方案 A: 在 `logback-spring.xml` 中添加 `default` profile 配置 → 缺点：仍然依赖 Spring
- 方案 B: 使用系统属性指定配置文件 → 缺点：需要修改 Maven surefire 配置

### Decision 2: H2 数据库配置标准化

**选择**: 使用 `MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE` 模式

**原因**:

- 与生产环境 MySQL 行为对齐
- 避免大小写敏感问题导致的测试失败
- 已验证可行（当前测试配置）

### Decision 3: 异常处理位置

**选择**: 在 `WebExceptionAdvise` 添加 `@ExceptionHandler(MethodArgumentNotValidException.class)`

**原因**:

- 符合全局异常处理的职责定位
- 所有 Controller 异常集中处理
- 符合现有代码模式

**备选方案**:

- 方案 A: 在每个 Controller 添加 `@Valid` 后手动处理 → 缺点：代码重复
- 方案 B: 使用 `@ControllerAdvice` 单独类 → 缺点：分散异常处理逻辑

### Decision 4: 配置默认值策略

**选择**: 在 `ApplicationBootstrap.java` 中为 `${server.servlet.context-path}` 提供默认值

**原因**:

- 确保测试环境配置缺失时不会报错
- 最小化配置变更影响

## Risks / Trade-offs

### Risk 1: 测试配置与生产配置不一致

- **风险**: `logback-test.xml` 与 `logback-spring.xml` 配置可能不同步
- **缓解**: 在 `test-standardization` spec 中添加配置同步检查要求

### Risk 2: H2 与 MySQL 行为差异

- **风险**: H2 的 MySQL 模式无法完全模拟 MySQL 行为
- **缓解**: 关键集成测试仍需在真实 MySQL 环境运行（CI/CD 阶段）

### Risk 3: 异常处理遗漏

- **风险**: 可能存在其他未覆盖的异常类型
- **缓解**: 在 `error-code-design` spec 中明确异常类型清单

## Migration Plan

### 阶段 1: 规范定义（本 Change）

1. 创建/更新相关 specs
2. 编写实现任务清单

### 阶段 2: 实现

1. 创建 `logback-test.xml`（已存在，需规范化）
2. 添加 `MethodArgumentNotValidException` 处理器（已存在，需规范化）
3. 清理技术债务

### 阶段 3: 验证

1. 运行完整测试套件验证 100% 通过率
2. 检查测试环境配置与规范一致性

## Open Questions

1. **CI/CD 环境配置**: CI/CD 流水线是否需要独立的配置文件？（建议：使用 `application-ci.yaml`）
2. **集成测试数据库**: 是否需要在 CI 中启动真实 MySQL 容器进行集成测试？（建议：使用 Testcontainers）
