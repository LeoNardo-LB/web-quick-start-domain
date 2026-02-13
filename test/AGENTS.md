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

### 覆盖率标准

| 指标    | 单元测试 | 集成测试 |
|-------|------|------|
| 行覆盖率  | ≥95% | ≥90% |
| 分支覆盖率 | ≥95% | ≥80% |
| 通过率   | 100% | 100% |

### TDD 验证流程（NON-NEGOTIABLE）

> **编码前必须加载 `tdd-workflow` skill**：`/tdd-workflow`

**五阶段验证流程**：

| 阶段       | 命令/操作                                                          | 说明           |
|----------|----------------------------------------------------------------|--------------|
| 1. 编码验证  | `mvn clean compile`                                            | 确保编译通过       |
| 2. 单元测试  | `python scripts/python/run-unit-tests.py --diff HEAD~1`        | 仅变更关联的单元测试   |
| 3. 集成测试  | `python scripts/python/run-integration-tests.py --diff HEAD~1` | 仅变更关联的集成测试   |
| 4. 覆盖率验证 | `mvn verify -pl test`                                          | 生成 JaCoCo 报告 |
| 5. 抽检测试  | `python scripts/python/run-sample-tests.py`                    | 所有测试中抽取 10%  |

**报告位置**：`test/target/site/jacoco/index.html`

### 禁止行为

- ❌ 直接使用 `mvn test` 运行测试（必须使用 TDD 脚本）
- ❌ 跳过技能加载直接编码
- ❌ 删除失败测试以通过构建
- ❌ 提交失败的测试代码

## 反模式（禁止）

| ❌ 禁止                                       | ✅ 正确做法                 |
|--------------------------------------------|------------------------|
| 在业务模块内包含测试                                 | 所有测试在独立 test/ 模块       |
| 单元测试启动 Spring                              | 纯 Mock，继承 UnitTestBase |
| 单元测试依赖外部系统                                 | 纯 Mock，禁止外部依赖          |
| `cases/` 下创建非 unittest/integrationtest 子目录 | 特殊测试放在 `unittest/` 子包  |

## 验证命令

```bash
# 单元测试（仅变更文件）
python scripts/python/run-unit-tests.py --diff HEAD~1

# 集成测试（仅变更文件）
python scripts/python/run-integration-tests.py --diff HEAD~1

# 启动验证
mvn test -Dtest=ApplicationStartupTests -pl test

# 覆盖率报告
mvn verify -pl test
# 报告: test/target/site/jacoco/index.html
```

---
**版本**: 2.0 | **整合自**: CONSTITUTION.md §XVII/§XVIII/§XIX/§XXXII
