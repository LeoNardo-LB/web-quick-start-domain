## Why

测试失败分析显示存在多个系统性问题：测试环境配置与生产环境耦合（MySQL vs H2、logback-spring.xml 依赖 Spring Profile）、异常处理不完整（缺少 Bean
Validation 异常处理）、以及技术债务（过时的 @Disabled 注解未清理）。这些问题导致测试通过率不稳定，增加了维护成本。

## What Changes

### 测试环境配置改进

- 创建 `logback-test.xml` 测试专用配置（不依赖 Spring Profile）
- 标准化 H2 内存数据库配置（MODE=MySQL、CASE_INSENSITIVE_IDENTIFIERS）
- 确保 `application.yaml` 中的配置项有合理默认值（如 context-path）

### 异常处理增强

- 在 `WebExceptionAdvise` 中添加 `MethodArgumentNotValidException` 处理器
- 统一参数校验失败的 HTTP 响应状态码（400 Bad Request）
- 确保所有 Bean Validation 异常返回友好的错误消息

### 技术债务清理

- 移除过时的 `@Disabled` 注解和误导性注释
- 更新 `LoggingComplianceUTest` 中的注释（移除"编译错误导致无法加载"的过时说明）

## Capabilities

### New Capabilities

- `test-environment-config`: 测试环境配置规范，包括数据库配置、日志配置、环境隔离策略

### Modified Capabilities

- `error-code-design`: 扩展 HTTP 状态码使用规范，明确 Bean Validation 异常返回 400 Bad Request（当前 spec 已定义 400 用于"客户端错误"，但未明确
  Bean Validation 场景）

- `test-standardization`: 补充测试环境配置要求（当前 spec 仅涵盖命名和目录结构，缺少环境配置规范）

## Impact

### 代码影响

- `test/src/test/resources/logback-test.xml` - 新增文件
- `test/src/test/resources/application.yaml` - 配置标准化
- `adapter/.../WebExceptionAdvise.java` - 添加异常处理器
- `test/.../LoggingComplianceUTest.java` - 清理过时注释

### 依赖影响

- 无新增外部依赖
- 测试环境继续使用 H2 数据库

### 系统影响

- 测试环境与生产环境配置解耦
- 提高测试通过率的稳定性
- 减少因配置问题导致的测试失败
