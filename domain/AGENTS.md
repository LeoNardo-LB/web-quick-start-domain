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
**版本**: 3.1 | **更新**: 2026-02-18
