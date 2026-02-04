# TEST 模块

独立测试模块 - 启动验证、单元测试、集成测试。

## 文档导航

### 🔗 相关文档

| 文档                                             | 用途                | 读者      |
|------------------------------------------------|-------------------|---------|
| **[项目知识库](../AGENTS.md)**                      | 项目架构概览和架构偏差分析     | 开发者、架构师 |
| **[AI 开发指南](../CLAUDE.md)**                    | AI 开发元指南          | 开发者、AI  |
| **[项目 README](../README.md)**                  | 项目概览和快速开始         | 开发者、架构师 |
| [Domain 层指南](domain/AGENTS.md)                 | 领域层核心业务逻辑和约定      | 后端开发者   |
| [Infrastructure 层指南](infrastructure/AGENTS.md) | 基础设施层技术实现和约定      | 后端开发者   |
| [Application 层指南](app/AGENTS.md)               | 应用层 CQRS 和用例编排    | 后端开发者   |
| [Adapter 层指南](adapter/AGENTS.md)               | 接口层 REST 控制器和事件监听 | 后端开发者   |
| [Start 模块指南](start/AGENTS.md)                  | 启动模块 Bean 装配和配置   | 后端开发者   |

### 🔗 规格文档

- [验证流程指南](_docs/specification/业务代码生成(AI)流程.md)
- [业务代码编写规范](_docs/specification/业务代码编写规范.md)
- [测试代码编写与示例指南](_docs/specification/测试代码编写与示例指南.md)

### 🔗 业务文档

- [业务文档索引](_docs/business/README.md)

## 概述

职责：独立测试模块，所有测试代码必须在此。
依赖规则：依赖所有业务模块，无反向依赖。
规模：30+ Java 文件，独立测试架构。

## 结构

```
test/
├── src/test/java/org/smm/archetype/test/
│   ├── ApplicationStartupTests.java  # Spring 上下文启动验证（守门员）
│   ├── TestBootstrap.java           # 测试专用启动类
│   ├── support/                   # 测试基类
│   │   ├── UnitTestBase.java     # 纯单元测试基类（0ms 启动）
│   │   └── ITestBase.java        # 集成测试基类（WebTestClient）
│   └── cases/                    # 测试用例
│       ├── unittest/             # 单元测试（*UTest）
│       └── integrationtest/       # 集成测试（*ITest）
└── src/test/resources/
    ├── config/                    # 测试配置（application-integration.yaml）
    ├── datasets/                  # DBUnit 数据集（integration/*.xml）
    └── schema.sql                 # DDL 文件（只读，禁止修改）
```

## 关键位置

| 任务     | 位置                                                                      | 备注                          |
|--------|-------------------------------------------------------------------------|-----------------------------|
| 启动验证   | test/.../ApplicationStartupTests.java                                   | Spring 上下文健康守门员             |
| 测试启动类  | test/.../TestBootstrap.java                                             | 独立 @SpringBootApplication 类 |
| 单元测试基类 | test/.../support/UnitTestBase.java            # 纯 Mock，0ms 启动           |
| 集成测试基类 | test/.../support/ITestBase.java              # Spring 上下文，WebTestClient |
| 单元测试   | test/.../cases/unittest/**/*UTest.java      # 如 OrderAggrUTest          |
| 集成测试   | test/.../cases/integrationtest/**/*ITest.java  # 如 ObservabilityITest   |
| 测试数据集  | test/src/test/resources/datasets/integration/  # DBUnit XML 数据文件        |

## 约定

独立测试模块：所有测试代码必须在 test/，业务模块禁止包含 src/test。
测试通过率：100%、行覆盖率≥95%、分支覆盖率100%、DDL 修改次数0。

## 测试基类

**UnitTestBase**：纯单元测试，0ms 启动，Mockito 模拟所有依赖，`@ExtendWith(MockitoExtension.class)`。

**ITestBase**：集成测试，启动 Spring 上下文（~200-500ms），WebTestClient，`@Transactional` 回滚，`@SpringBootTest(RANDOM_PORT)`。

## 独特模式

**启动验证守门员**：Spring 上下文健康检查（`mvn test -Dtest=ApplicationStartupTests -pl test`）。

**双 @SpringBootApplication**：`ApplicationBootstrap`（生产）、`TestBootstrap`（测试，仅扫描业务包）。

**禁止 @Disabled**：单元测试核心原则是 Mock 隔离依赖，需要集成环境的测试放 *IntegrationTest。

**DBUnit 强制使用**：所有集成测试数据必须通过 DBUnit XML 加载（禁止在测试代码中创建数据）。

**TDD 流程**：Phase 0（规划）→ Phase 1（编写测试）→ Phase 2（编译）→ Phase 3（单元测试）→ Phase 4（启动验证）。

## 反模式

❌ 业务模块 src/test、业务模块添加测试依赖、业务模块依赖 test 模块、单元测试使用 `@SpringBootTest`/`@Autowired`、直接操作数据库、执行 DDL、使用
`@Disabled`、修改 schema.sql。

## 命名约定

| 类型   | 命名模式                              | 示例                                      |
|------|-----------------------------------|-----------------------------------------|
| 单元测试 | `XxxUTest`                        | OrderAggrUTest                          |
| 集成测试 | `XxxITest` 或 `XxxIntegrationTest` | ObservabilityITest                      |
| 包名   | `cases.unittest.{groupId}`        | cases.unittest.org.smm.archetype        |
| 包名   | `cases.integrationtest.{groupId}` | cases.integrationtest.org.smm.archetype |
