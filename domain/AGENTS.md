# Domain 层 - DDD 核心层

**纯净业务逻辑层**：零外部依赖，包含聚合根、实体、值对象、仓储接口、领域服务。

## 目录结构

```
domain/src/main/java/org/smm/archetype/domain/
├── shared/             # 共享领域对象
│   ├── base/           # 基类：AggregateRoot、ValueObject、Entity、Command、Query
│   ├── client/         # 技术客户端接口（CacheClient、OssClient、SearchClient）
│   └── event/          # 领域事件基类
├── platform/           # 平台能力（file、search、audit）
└── {模块}/             # 业务模块
    ├── model/          # 聚合根、实体、值对象、枚举
    ├── repository/     # 仓储接口
    └── service/        # 领域服务
```

## 关键查找

| 目标   | 位置                                       |
|------|------------------------------------------|
| 聚合根  | `{模块}/model/*Aggr.java`                  |
| 值对象  | `{模块}/model/valueobject/*.java`          |
| 仓储接口 | `{模块}/repository/*Repository.java`       |
| 领域枚举 | `domain/platform/enums/` 或 `{模块}/model/` |
| 领域事件 | `{模块}/event/*.java`                      |

## 核心规则

### 纯净性约束（NON-NEGOTIABLE）

| 规则        | 说明                                              |
|-----------|-------------------------------------------------|
| 依赖方向      | Adapter → Application → Domain ← Infrastructure |
| Domain 纯净 | **零外部依赖**，仅包含纯业务逻辑                              |
| 聚合根       | 唯一允许通过 Repository 持久化的领域对象                      |
| 值对象       | 必须不可变，通过属性值判断相等性                                |

**禁止引入**：Spring 框架、`@Transactional`、`@Autowired`、Infrastructure 实现类

### 聚合根模式

```java
public class OrderAggr extends AggregateRoot<OrderAggr, OrderId> {
    private OrderId id;
    private OrderStatus status;
    
    // 静态工厂创建
    public static OrderAggr create(String customerId, Money totalAmount) {
        OrderAggr order = new OrderAggr();
        order.id = OrderId.generate();
        order.status = OrderStatus.CREATED;
        order.recordEvent(new OrderCreatedEvent(order.id)); // 记录事件
        return order;
    }
    
    // 业务方法：验证 → 修改 → 发布事件
    public void pay(PaymentMethod method) {
        if (!status.canPay()) throw new BizException("状态不允许支付");
        this.status = OrderStatus.PAID;
        this.recordEvent(new OrderPaidEvent(this.id));
    }
}
```

### 值对象模式

```java
@AllArgsConstructor @FieldDefaults(makeFinal = true, level = PRIVATE)
public class Money extends ValueObject {
    private BigDecimal amount;
    private String currency;
    
    // 运算返回新实例（不可变）
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) throw new IllegalArgumentException("币种不同");
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### 枚举状态机模式

| 模式   | 说明                          |
|------|-----------------------------|
| 状态判断 | 枚举提供 `canXxx()` 方法          |
| 双向映射 | `code` 字段 + `fromCode()` 方法 |

**字段含 type/status/state/source/business/errorCode/level/mode 时必须使用枚举。**

## 禁止

| ❌ 禁止                | ✅ 正确                                       |
|---------------------|--------------------------------------------|
| 使用 `@Transactional` | 事务边界在 Application 层                        |
| 使用 `@Data` 注解       | 使用 `@Builder` + `@RequiredArgsConstructor` |
| 跨聚合根直接调用方法          | 通过领域事件异步处理                                 |
| 字段使用 String 存储枚举值   | 使用枚举类型                                     |

---
← [项目知识库](../AGENTS.md) | **版本**: 3.3 | **更新**: 2026-02-19
