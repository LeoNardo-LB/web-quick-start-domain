# Domain 层 - DDD 核心层

**纯净业务逻辑层**：零外部依赖，包含聚合根、实体、值对象、仓储接口、领域服务、领域事件。

## 目录结构

```
domain/src/main/java/org/smm/archetype/domain/
├── shared/             # 共享领域对象
│   ├── base/           # 基类：AggregateRoot、ValueObject、Entity、Command、Query、BaseRepository
│   ├── client/         # 技术客户端接口：CacheClient、OssClient、SearchClient
│   ├── event/          # 领域事件基类
│   └── util/           # 工具类（MyBeanUtil、CacheHolder）
├── common/             # 通用领域对象
│   ├── file/           # 文件管理
│   ├── search/         # 搜索服务接口
│   └── enums/          # 共享枚举（多模块使用）
└── {模块}/             # 业务模块
    ├── model/          # 聚合根、实体、值对象、枚举
    │   └── valueobject/  # 值对象目录
    ├── repository/     # 仓储接口
    └── service/        # 领域服务
```

## 关键查找

| 目标   | 位置                                     | 说明                         |
|------|----------------------------------------|----------------------------|
| 聚合根  | `{模块}/model/*Aggr.java`                | 继承 `AggregateRoot<T, ID>`  |
| 值对象  | `{模块}/model/valueobject/*.java`        | 继承 `ValueObject`           |
| 仓储接口 | `{模块}/repository/*Repository.java`     | 继承 `BaseRepository<T, ID>` |
| 领域枚举 | `domain/common/enums/` 或 `{模块}/model/` | 多模块共享放 common              |
| 领域事件 | `{模块}/event/*.java`                    | 继承 `DomainEvent`           |

## 核心规则

### 四层架构原则（NON-NEGOTIABLE）

| 规则        | 说明                                              |
|-----------|-------------------------------------------------|
| 依赖方向      | Adapter → Application → Domain ← Infrastructure |
| Domain 纯净 | **零外部依赖**，仅包含纯业务逻辑                              |
| 聚合根       | 唯一允许通过 Repository 持久化的领域对象                      |
| 值对象       | 必须不可变，通过属性值判断相等性                                |
| 领域事件      | 必须通过 `recordEvent()` 记录，禁止跨聚合根直接调用              |

### 纯净性约束

**禁止引入**：

- ❌ Spring 框架工具（`BeanUtils`、`FastJSON`、`@Transactional`）
- ❌ Infrastructure 层实现类（仅通过接口声明）
- ❌ `@Autowired` 注入（通过构造函数注入，在 Application 层）

**允许**：

- ✅ `bizshared/client/` 定义技术客户端接口（由 Infrastructure 层实现）

### 聚合根规范

```java
public class OrderAggr extends AggregateRoot<OrderAggr, OrderId> {
    private OrderId id;
    private OrderStatus status;
    
    // 静态工厂方法创建
    public static OrderAggr create(String customerId, Money totalAmount) {
        OrderAggr order = new OrderAggr();
        order.id = OrderId.generate();
        order.status = OrderStatus.CREATED;
        order.recordEvent(new OrderCreatedEvent(order.id)); // 记录事件
        return order;
    }
    
    // 业务方法修改状态
    public void pay(PaymentMethod method) {
        if (this.status != OrderStatus.CREATED) {
            throw new BizException("只有已创建的订单可以支付");
        }
        this.status = OrderStatus.PAID;
        this.recordEvent(new OrderPaidEvent(this.id));
    }
}
```

### 值对象规范

| 规则  | 实现                                  |
|-----|-------------------------------------|
| 不可变 | 使用 `@Builder` + `final` 字段          |
| 相等性 | 重写 `equals()` 和 `hashCode()`，按属性值判断 |
| 无标识 | 无 ID，由聚合根或实体持有                      |

```java
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class Money extends ValueObject {
    private BigDecimal amount;
    private String currency;
    
    // 值对象操作返回新实例
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("币种不同");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### 枚举设计原则

| 场景         | 位置                        | 可见性             |
|------------|---------------------------|-----------------|
| 单模块使用      | `{模块}/model/XxxEnum.java` | 内部类或独立类         |
| 多模块共享      | `domain/common/enums/`    | `public`        |
| 领域契约（跨层引用） | 独立文件                      | **必须 `public`** |

**字段语义含 type/status/state/source/business/errorCode/level/mode 时必须使用枚举。**

**枚举转换**：Domain ↔ DO 转换在 Infrastructure 层 Converter 中完成。

### 高内聚原则

| 场景    | 可见性                       | 示例                                   |
|-------|---------------------------|--------------------------------------|
| 完全不暴露 | `private`                 | `CacheValueWrapper`、`CaffeineExpiry` |
| 只读暴露  | `public` getter，禁止 setter | 值对象、record                           |
| 完全暴露  | `public` 独立类              | `ContextRunnable`、领域枚举               |

**禁止内部化**：被多类型引用、需独立测试、作为公共 API 的类型。

## 反模式（禁止）

| ❌ 禁止                            | ✅ 正确做法                                     |
|---------------------------------|--------------------------------------------|
| 使用 `@Transactional`             | 事务边界在 Application 层                        |
| 使用 `BeanUtils.copyProperties()` | MapStruct 转换（Infrastructure 层）             |
| 跨聚合根直接调用方法                      | 通过领域事件异步处理                                 |
| 使用 `@Data` 注解                   | 使用 `@Builder` + `@RequiredArgsConstructor` |
| 字段使用 String 存储枚举值               | 使用枚举类型                                     |
| 内部类过度膨胀                         | 独立类放在 common/enums/                        |

## Order Demo 设计模式

以下模式提取自 `exampleorder` 模块，体现 DDD 核心设计思想。

### 聚合根设计模式

**核心思想**：聚合根是领域模型的唯一入口，封装业务规则、状态流转和事件发布。

**关键设计点**：

| 模式        | 说明                         | 示例                                               |
|-----------|----------------------------|--------------------------------------------------|
| 静态工厂      | `create()` 封装创建逻辑，确保初始状态正确 | `OrderAggr.create(customerId, totalAmount)`      |
| 状态验证      | 业务方法先验证状态（委托给枚举），再修改       | `if (!status.canPay()) throw ...`                |
| 事件驱动      | 状态变更后发布领域事件，解耦聚合间通信        | `publishPaidEvent(); recordEvent(event)`         |
| Builder命名 | 自定义构建器名，避免与父类冲突            | `@SuperBuilder(builderMethodName = "OABuilder")` |

**业务方法结构**：

```
验证前置条件 → 修改状态 → 发布事件 → 标记更新
```

### 值对象设计模式

**核心思想**：值对象通过属性值标识，必须不可变，运算返回新实例。

**关键设计点**：

| 模式   | 说明                    | 示例                                |
|------|-----------------------|-----------------------------------|
| 不可变性 | `final` 字段 + 私有构造     | `private final BigDecimal amount` |
| 静态工厂 | 提供语义化创建方法             | `Money.of(100)`, `Money.zero()`   |
| 运算不变 | 算术方法返回新实例             | `add()` 返回新 Money，不修改原对象          |
| 业务规则 | 内置校验逻辑                | `assertSameCurrency()` 检查币种一致性    |
| 值相等  | 重写 `equalityFields()` | 基于 amount + currency 判断相等         |

### 枚举状态机模式

**核心思想**：枚举封装状态流转规则，聚合根委托枚举进行状态验证。

**关键设计点**：

| 模式   | 说明                          | 示例                               |
|------|-----------------------------|----------------------------------|
| 状态判断 | 枚举提供 `canXxx()` 方法          | `CREATED.canPay() → true`        |
| 双向映射 | `code` 字段 + `fromCode()` 方法 | 支持数据库存储与领域转换                     |
| 业务语义 | 枚举值命名反映业务状态                 | `CREATED/PAID/SHIPPED/COMPLETED` |

### 领域事件 DTO 模式

**核心思想**：事件 DTO 携带业务关键信息，避免暴露聚合内部结构。

**关键设计点**：

| 模式        | 说明               | 示例                                                |
|-----------|------------------|---------------------------------------------------|
| 继承基类      | 统一事件元数据          | `extends DomainEventDTO`                          |
| Builder命名 | 自定义构建器名          | `@SuperBuilder(builderMethodName = "OCEBuilder")` |
| 嵌套简化      | 使用 `record` 简化负载 | `record OrderItemInfo(productId, quantity)`       |
| 信息脱敏      | 仅包含必要业务字段        | 不暴露聚合内部方法                                         |

### 仓储接口模式

**核心思想**：仓储返回领域对象，方法命名遵循领域语言。

**方法分类**：

| 类型   | 方法示例                            | 说明              |
|------|---------------------------------|-----------------|
| 写操作  | `save(aggregate)`               | 持久化聚合根          |
| 单条查询 | `findById()`, `findByOrderNo()` | 返回聚合根或 null     |
| 列表查询 | `findByCustomerId()`            | 返回聚合根列表         |
| 分页查询 | `findOrders(query)`             | 返回 `PageResult` |
| 存在性  | `existsByOrderNo()`             | 布尔值，用于唯一性校验     |

## 模块边界

### 对外暴露

| 类型      | 位置                                 | 说明     |
|---------|------------------------------------|--------|
| 聚合根     | `{模块}/model/*Aggr.java`            | 业务逻辑入口 |
| 值对象     | `{模块}/model/valueobject/*.java`    | 不可变值   |
| 仓储接口    | `{模块}/repository/*Repository.java` | 持久化抽象  |
| 领域事件    | `{模块}/event/*.java`                | 事件定义   |
| 技术客户端接口 | `shared/client/*Client.java`       | 基础设施接口 |

### 依赖下游

| 模块 | 依赖方式 | 说明            |
|----|------|---------------|
| 无  | -    | Domain 层零外部依赖 |

### 禁止

- ❌ 依赖 Spring 框架（`@Transactional`、`@Autowired`）
- ❌ 依赖 Infrastructure 实现（仅通过接口声明）
- ❌ 使用 `@Data` 注解
- ❌ 引入 MyBatis/JPA 注解

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
