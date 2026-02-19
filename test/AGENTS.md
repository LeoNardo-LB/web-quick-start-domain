# Test 模块 - 独立测试模块

**独立测试模块**：单元测试、集成测试、测试基类。测试代码与业务代码完全隔离。

## 目录结构

```
test/src/test/java/org/smm/archetype/test/
├── support/            # 测试基类
│   ├── UnitTestBase.java         # 单元测试基类（纯 Mock）
│   └── IntegrationTestBase.java  # 集成测试基类（@SpringBootTest）
├── cases/              # 测试用例
│   ├── unittest/               # 单元测试
│   └── integrationtest/        # 集成测试
└── resources/          # 测试资源
    └── application.yaml
```

## 关键查找

| 目标   | 位置                                                               |
|------|------------------------------------------------------------------|
| 单元测试 | `cases/unittest/**/*UTest.java`                                  |
| 集成测试 | `cases/integrationtest/**/*ITest.java`                           |
| 测试基类 | `support/UnitTestBase.java` / `support/IntegrationTestBase.java` |

## 核心规则

### 测试环境配置（NON-NEGOTIABLE）

| 规则               | 说明                                           |
|------------------|----------------------------------------------|
| 独立配置             | 测试环境使用独立的配置文件，与生产环境解耦                        |
| H2 数据库           | 测试环境使用 H2 内存数据库，配置 MySQL 兼容模式                |
| logback-test.xml | 单元测试使用 `logback-test.xml`，不依赖 Spring Profile |

### 命名规范

| 类型   | 命名格式                 | 示例                       |
|------|----------------------|--------------------------|
| 单元测试 | `XxxUTest`           | `OrderAppServiceUTest`   |
| 集成测试 | `XxxITest`           | `OrderControllerITest`   |

### 单元测试规范（NON-NEGOTIABLE）

| 规则        | 说明                                   |
|-----------|--------------------------------------|
| 纯 Mock    | 必须使用 Mockito 模拟所有依赖                  |
| 禁止 Spring | 不得启动 Spring 上下文，禁止 `@SpringBootTest` |
| 基类        | 必须继承 `UnitTestBase`                  |
| 通过率       | **100%**                             |

```java
class OrderAppServiceUTest extends UnitTestBase {
    @Mock
    private OrderAggrRepository orderRepository;
    
    @Test
    void testCreateOrder() {
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        OrderId orderId = orderAppService.create(command);
        assertNotNull(orderId);
    }
}
```

### 集成测试规范（NON-NEGOTIABLE）

| 规则         | 说明                                          |
|------------|---------------------------------------------|
| Spring 上下文 | 必须使用 `@SpringBootTest` 启动完整上下文              |
| 基类         | 必须继承 `IntegrationTestBase`                  |
| 验证范围       | 验证完整业务流程（Controller → Service → Repository） |

### TDD 验证流程（NON-NEGOTIABLE）

| 阶段     | 触发时机    | 命令                                                 |
|--------|---------|----------------------------------------------------|
| LSP 检查 | 每个文件完成后 | `lsp_diagnostics`                                  |
| 编译验证   | 模块实现完成后 | `mvn clean compile`                                |
| 单元测试   | 模块测试阶段  | `mvn test -Dtest=XxxUTest -pl test`                |
| 集成测试   | 所有模块完成后 | `mvn test -Dtest=XxxITest -pl test`                |
| 启动验证   | 所有测试通过后 | `mvn test -Dtest=ApplicationStartupTests -pl test` |

### 禁止

| ❌ 禁止          | ✅ 正确做法                 |
|---------------|------------------------|
| 在业务模块内包含测试    | 所有测试在独立 test/ 模块       |
| 单元测试启动 Spring | 纯 Mock，继承 UnitTestBase |
| 单元测试依赖外部系统    | 纯 Mock，禁止外部依赖          |
| 删除失败测试以通过构建   | 修复测试                   |

---
← [项目知识库](../AGENTS.md) | **版本**: 3.3 | **更新**: 2026-02-19
