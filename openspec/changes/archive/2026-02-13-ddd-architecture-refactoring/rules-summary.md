# DDD 架构重构 - 规则摘要

**归档日期**: 2026-02-13
**变更名称**: ddd-architecture-refactoring
**Schema**: spec-driven

## 一、包结构规范

### 1.1 四层架构包命名

| 层级 | 旧命名 | 新命名 | 说明 |
|------|--------|--------|------|
| Domain | `bizshared/`, `common/`, `example/` | `shared/`, `platform/`, `exampleorder/` | 领域层纯净 |
| App | `bizshared/`, `example/` | `shared/`, `exampleorder/` | 应用层 |
| Infrastructure | `bizshared/`, `common/`, `example/` | `shared/`, `platform/`, `exampleorder/` | 基础设施层 |
| Adapter | `bizshared/`, `example/` | `shared/`, `exampleorder/` | 接口适配层 |

### 1.2 命名规范

| 类型 | 旧名称 | 新名称 |
|------|--------|--------|
| 上下文工具 | `MyContext` | `ScopedThreadContext` |
| 日志注解 | `@MyLog` | `@BusinessLog` |
| IP工具 | `MyIpUtil` | `IpAddressUtil` |
| 日志实体 | `Log.java` | `MethodExecutionLog.java` |

---

## 二、异常处理规范

### 2.1 异常类型

| 异常类型 | 使用场景 | 父类 |
|----------|----------|------|
| `BizException` | 业务异常（用户可理解的错误） | `RuntimeException` |
| `SysException` | 系统异常（技术错误） | `RuntimeException` |
| `ClientException` | 外部客户端调用异常 | `RuntimeException` |

### 2.2 错误码规范

```java
// 错误码必须使用枚举类型
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND("ORDER-001", "订单不存在"),
    ORDER_ITEMS_EMPTY("ORDER-002", "订单项为空"),
    // ...
}

// 枚举必须实现 ErrorCode 接口
public interface ErrorCode {
    String getCode();
    String getMessage();
}
```

### 2.3 BaseResult 字段类型

```java
// code 字段必须是 String 类型，支持业务错误码格式
protected String code;
```

---

## 三、DTO 转换规范

### 3.1 MapStruct 强制使用

**所有 DTO 转换必须使用 MapStruct**，禁止手动转换。

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderDtoConverter {
    OrderDTO toDTO(OrderAggr order);
    List<OrderDTO> toDTOList(List<OrderAggr> orders);
}
```

### 3.2 转换器位置

| 转换器 | 位置 |
|--------|------|
| `OrderRequestConverter` | `adapter/exampleorder/converter/` |
| `OrderResponseConverter` | `adapter/exampleorder/converter/` |
| `OrderDtoConverter` | `app/exampleorder/converter/` |

### 3.3 禁止静态转换方法

```java
// ❌ 禁止
public static OrderResponse fromDTO(OrderDTO dto) { ... }

// ✅ 使用 MapStruct
@Mapper(componentModel = "spring")
public interface OrderResponseConverter {
    OrderResponse toResponse(OrderDTO dto);
}
```

---

## 四、日志规范

### 4.1 标准日志格式

```
[类型] 类#方法 | 业务描述 | 耗时ms | 线程 | 入参 | 出参/错误
```

### 4.2 LogAspect 自动添加元数据

LogAspect 已实现自动添加：
- 类名、方法名
- 执行耗时
- 线程名称
- 入参/出参

### 4.3 traceId 实现

```java
// ContextFillFilter 自动生成/传递 traceId
String traceId = request.getHeader("X-Trace-Id");
if (traceId == null || traceId.isEmpty()) {
    traceId = ScopedThreadContext.generateTraceId();
}

// BaseResult 自动获取 traceId
String traceId = ScopedThreadContext.getTraceId();
this.traceId = traceId != null ? traceId : "N/A";
```

---

## 五、TDD 开发流程规范

### 5.1 分层递进验证

| 阶段 | 触发条件 | 操作 | 通过标准 |
|------|----------|------|----------|
| **1. 正常编码** | 编写代码时 | 仅检查 LSP 错误 | 无 LSP 报错 |
| **2. 待办完成** | todolist 完成后 | 生成单元测试 + 编译 + 单测 | 单测通过率 100% |
| **3. 阶段完成** | 95%+ 代码完成后 | 生成集成测试 + 编译 + 测试 | 集测通过率 100% |
| **4. 覆盖率验证** | 测试通过后 | 运行覆盖率检查 | 覆盖率 ≥80% |
| **5. 最终抽检** | 覆盖率达标后 | 抽检 10% 单测 + 10% 集测 | 抽检通过率 100% |

### 5.2 详细规则

**阶段 2：待办完成 → 生成单元测试**
- 触发条件：todolist 所有任务完成 + LSP 无报错
- 执行步骤：
  1. 为本次修改的代码生成单元测试
  2. 使用 assert 断言覆盖所有分支
  3. 执行 `mvn compile -q` 编译验证
  4. 执行 `mvn test -Dtest=*UTest` 运行单元测试
- 通过标准：**单元测试通过率 100%**

**阶段 3：阶段完成 → 生成集成测试**
- 触发条件：预估完成度 ≥95% + 单元测试全部通过
- 执行步骤：
  1. 为本次修改的代码生成集成测试
  2. 执行 `mvn compile -q` 编译验证
  3. 执行 `mvn test -Dtest=*ITest` 运行集成测试
- 通过标准：**集成测试通过率 100%**

**阶段 5：最终抽检**
- 随机抽取 **10% 的单元测试用例**
- 随机抽取 **10% 的集成测试用例**
- 通过标准：**抽检的单测和集测通过率均为 100%**

### 5.3 验证命令

```bash
# 阶段 2：单元测试
mvn compile -q
mvn test -Dtest=*UTest

# 阶段 3：集成测试
mvn test -Dtest=*ITest

# 阶段 4：覆盖率
mvn verify -pl test

# 阶段 5：抽检（示例）
mvn test -Dtest=OrderAppServiceUTest,OrderControllerITest

# 启动验证
mvn test -Dtest=ApplicationStartupTests -pl test
```

---

## 六、配置集中化规范

### 6.1 配置类位置

所有配置类必须在 `start/src/main/java/org/smm/archetype/config/` 目录。

### 6.2 命名规范

配置类命名格式：`{Aggregate}Configure`

```java
// 例如
OrderConfigure.java
EventConfigure.java
ThreadPoolConfigure.java
```

### 6.3 Bean 装配

```java
@Configuration
public class OrderConfigure {
    
    @Bean
    public OrderAppService orderAppService(
            OrderAggrRepository orderRepository,
            OrderDomainService orderDomainService,
            DomainEventPublisher domainEventPublisher,
            OrderDtoConverter dtoConverter) {  // 注入 MapStruct 转换器
        return new OrderAppService(orderRepository, orderDomainService, 
                                   domainEventPublisher, dtoConverter);
    }
}
```

---

## 七、依赖隔离规范

### 7.1 禁止项

| 禁止 | 原因 |
|------|------|
| `@Lazy` | 掩盖循环依赖 |
| `ObjectProvider` | 延迟注入 |
| Domain 层使用 Spring 工具 | 破坏纯净性 |

### 7.2 循环依赖解决

必须通过重构解决，不得使用延迟注入。

---

## 八、测试规范

### 8.1 测试模块独立

所有测试代码必须在独立的 `test/` 模块中。

### 8.2 命名规范

| 类型 | 命名格式 | 基类 |
|------|----------|------|
| 单元测试 | `XxxUTest` | `UnitTestBase` |
| 集成测试 | `XxxITest` | `IntegrationTestBase` |

### 8.3 覆盖率要求

| 测试类型 | 行覆盖率 | 分支覆盖率 |
|----------|----------|------------|
| 单元测试 | ≥95% | ≥95% |
| 集成测试 | ≥90% | ≥80% |

---

## 九、新增/修改的文件清单

### 新增文件

| 文件 | 说明 |
|------|------|
| `ScopedThreadContext.java` | 线程上下文（重命名自 MyContext） |
| `BusinessLog.java` | 业务日志注解（重命名自 @MyLog） |
| `MethodExecutionLog.java` | 日志实体（重命名自 Log.java） |
| `IpAddressUtil.java` | IP 工具（重命名自 MyIpUtil） |
| `OrderDtoConverter.java` | MapStruct DTO 转换器 |
| `OrderRequestConverter.java` | MapStruct Request 转换器 |
| `OrderResponseConverter.java` | MapStruct Response 转换器 |

### 关键修改文件

| 文件 | 变更 |
|------|------|
| `BaseResult.java` | 添加 traceId 自动获取 |
| `ContextFillFilter.java` | 生成/传递 traceId |
| `OrderController.java` | 使用 MapStruct 转换器 |
| `OrderAppService.java` | 使用 MapStruct 转换器 |
| `OrderConfigure.java` | 注入转换器依赖 |
| `constitution.md` | 添加 TDD 开发流程规范（v1.10.0） |

---

## 十、验证结果

| 验证项 | 结果 |
|--------|------|
| `mvn clean compile` | ✅ 通过 |
| `mvn test` | ✅ 77 tests, 0 failures |
| `ApplicationStartupTests` | ✅ 通过 |

---

**版本**: 1.0 | **归档人**: AI Assistant | **归档日期**: 2026-02-13
