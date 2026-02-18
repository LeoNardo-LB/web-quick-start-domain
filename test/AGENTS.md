# Test 模块 - 独立测试模块

**独立测试模块**：单元测试、集成测试、测试基类。测试代码与业务代码完全隔离。

## 目录结构

```
test/src/test/java/org/smm/archetype/test/
├── support/            # 测试基类
│   ├── UnitTestBase.java         # 单元测试基类（纯 Mock）
│   ├── IntegrationTestBase.java  # 集成测试基类（@SpringBootTest）
│   └── TestBootstrap.java        # 测试引导类
├── cases/              # 测试用例（仅包含两个子目录）
│   ├── unittest/               # 单元测试
│   │   ├── {业务模块}/           # 按业务模块组织
│   │   ├── performance/         # 性能测试（子包）
│   │   └── compliance/          # 合规测试（子包）
│   └── integrationtest/        # 集成测试
│       └── {业务模块}/           # 按业务模块组织
└── resources/          # 测试资源
    ├── application.yaml
    └── application-integration.yaml
```

## 关键查找

| 目标   | 位置                                                               | 说明                |
|------|------------------------------------------------------------------|-------------------|
| 单元测试 | `cases/unittest/**/*UTest.java`                                  | 纯 Mock，不启动 Spring |
| 集成测试 | `cases/integrationtest/**/*ITest.java`                           | @SpringBootTest   |
| 测试基类 | `support/UnitTestBase.java` / `support/IntegrationTestBase.java` | 继承使用              |
| 测试数据 | `resources/`                                                     | DBUnit XML/YAML   |

## 核心规则

### 测试环境配置（NON-NEGOTIABLE）

| 规则               | 说明                                           |
|------------------|----------------------------------------------|
| 独立配置             | 测试环境使用独立的配置文件，与生产环境解耦                        |
| H2 数据库           | 测试环境使用 H2 内存数据库，配置 MySQL 兼容模式                |
| logback-test.xml | 单元测试使用 `logback-test.xml`，不依赖 Spring Profile |

**H2 数据库配置** (`test/src/test/resources/application.yaml`)：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    driverClassName: org.h2.Driver
    username: sa
    password:
```

**Logback 测试配置** (`test/src/test/resources/logback-test.xml`)：

- 自动加载优先级高于 `logback-spring.xml`
- 不依赖 Spring Profile，单元测试可直接使用
- 包含所有必需 appender（CONSOLE、FILE、ASYNC_FILE、ASYNC_CURRENT、ERROR_FILE、AUDIT_FILE）

### 测试模块结构（NON-NEGOTIABLE）

| 规则         | 说明                                              |
|------------|-------------------------------------------------|
| 独立模块       | 所有测试代码必须在独立的 `test/` 模块中                        |
| 禁止         | 严禁在业务模块（domain、app、infrastructure、adapter）内包含测试 |
| cases/ 子目录 | 仅包含 `unittest` 和 `integrationtest` 两个子目录        |
| 特殊测试类型     | 放在 `unittest/` 子包下（如 `unittest/performance`）    |

### 命名规范

| 类型   | 命名格式                 | 示例                       |
|------|----------------------|--------------------------|
| 单元测试 | `XxxUTest`           | `OrderAppServiceUTest`   |
| 集成测试 | `XxxITest`           | `OrderControllerITest`   |
| 性能测试 | `XxxPerformanceTest` | `LoggingPerformanceTest` |

### 测试类型判断

| 场景                              | 测试类型     | 说明              |
|---------------------------------|----------|-----------------|
| 大入口类（Service、Facade、Controller） | **集成测试** | 测试多方调用的准确性与整合能力 |
| 简单类（工具类、中间件接入类）                 | **单元测试** | 测试简单功能实现        |

### 单元测试规范（NON-NEGOTIABLE）

| 规则        | 说明                                   |
|-----------|--------------------------------------|
| 纯 Mock    | 必须使用 Mockito 模拟所有依赖                  |
| 禁止 Spring | 不得启动 Spring 上下文，禁止 `@SpringBootTest` |
| 禁止外部依赖    | 不得依赖外部系统（数据库、消息队列、缓存等）               |
| 基类        | 必须继承 `UnitTestBase`                  |
| 通过率       | **100%**                             |

```java
class OrderAppServiceUTest extends UnitTestBase {
    @Mock
    private OrderAggrRepository orderRepository;
    
    @Test
    void testCreateOrder() {
        // Given
        CreateOrderCommand command = new CreateOrderCommand("customer123", new Money("100.00", "CNY"));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        // When
        OrderId orderId = orderAppService.create(command);
        
        // Then
        assertNotNull(orderId);
        verify(orderRepository, times(1)).save(any());
    }
}
```

### 集成测试规范（NON-NEGOTIABLE）

| 规则         | 说明                                          |
|------------|---------------------------------------------|
| Spring 上下文 | 必须使用 `@SpringBootTest` 启动完整上下文              |
| 基类         | 必须继承 `IntegrationTestBase`                  |
| 数据管理       | 使用 DBUnit，通过 XML/YAML 文件定义测试数据              |
| 验证范围       | 验证完整业务流程（Controller → Service → Repository） |
| 通过率        | **100%**                                    |

```java
class OrderControllerITest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateOrderApi() throws Exception {
        String requestBody = "{\"customerId\":\"customer123\",\"totalAmount\":{\"amount\":\"100.00\",\"currency\":\"CNY\"}}";
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

### TDD 验证流程（NON-NEGOTIABLE）

> **详细流程规范**: `openspec/config.yaml` 中的 TDD 验证流程章节

**四阶段验证流程**：

| 阶段        | 触发时机        | 命令                                  | 通过标准           |
|-----------|-------------|-------------------------------------|----------------|
| 1. LSP 检查 | 每个文件/小功能完成  | `lsp_diagnostics`                   | 零错误            |
| 2. 单元测试   | TODOLIST 完成 | `mvn test -Dtest=XxxUTest -pl test` | 100% 通过，分支全覆盖  |
| 3. 集成测试   | 所有开发完成      | `mvn test -Dtest=XxxITest -pl test` | 100% 通过，重要分支覆盖 |
| 4. 抽检     | 单测+集测通过后    | `mvn test -pl test`（抽检 10%）         | 100% 通过        |

**阶段提交原则**（防止 diff 过大）：

| 阶段完成     | 提交格式                                              |
|----------|---------------------------------------------------|
| 阶段 1（编码） | `feat(xxx): complete phase 1 - implementation`    |
| 阶段 2（单测） | `test(xxx): complete phase 2 - unit tests`        |
| 阶段 3（集测） | `test(xxx): complete phase 3 - integration tests` |
| 阶段 4（抽检） | `feat(xxx): complete TDD verification`            |

### 禁止行为

- ❌ 跳过阶段 2/3 直接进入阶段 4
- ❌ 跨多个阶段不提交代码（导致 diff 过大）
- ❌ 删除失败测试以通过构建
- ❌ 提交失败的测试代码
- ❌ 单元测试启动 Spring 上下文

## 反模式（禁止）

| ❌ 禁止                                       | ✅ 正确做法                 |
|--------------------------------------------|------------------------|
| 在业务模块内包含测试                                 | 所有测试在独立 test/ 模块       |
| 单元测试启动 Spring                              | 纯 Mock，继承 UnitTestBase |
| 单元测试依赖外部系统                                 | 纯 Mock，禁止外部依赖          |
| `cases/` 下创建非 unittest/integrationtest 子目录 | 特殊测试放在 `unittest/` 子包  |

## 验证命令

```bash
# 单元测试（指定类）
mvn test -Dtest=OrderAppServiceUTest -pl test

# 集成测试（指定类）
mvn test -Dtest=OrderControllerITest -pl test

# 启动验证
mvn test -Dtest=ApplicationStartupTests -pl test
```

---

## 相关文档

- [项目知识库](../AGENTS.md) - 架构概览和全局规范
- [Domain 层](../domain/AGENTS.md) - 领域层规范
- [Application 层](../app/AGENTS.md) - 应用层规范
- [Infrastructure 层](../infrastructure/AGENTS.md) - 基础设施层规范
- [Adapter 层](../adapter/AGENTS.md) - 接口层规范
- [Start 模块](../start/AGENTS.md) - 启动模块规范
- [Test 模块](../test/AGENTS.md) - 测试规范
- [TDD 流程](../openspec/config.yaml) - 四阶段验证流程

---
**版本**: 3.2 | **更新**: 2026-02-18
