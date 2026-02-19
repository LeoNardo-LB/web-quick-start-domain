# Application 层 - 应用服务层

**用例编排层**：CQRS 编排、DTO 转换、事务边界。

## 目录结构

```
app/src/main/java/org/smm/archetype/app/
├── shared/             # 共享应用服务
│   ├── event/          # 事件处理器（@Async 异步执行）
│   └── result/         # 统一响应（BaseResult、PageResult）
└── {模块}/             # 业务模块
    ├── command/        # 命令对象：{UseCase}Command
    ├── query/          # 查询对象：{UseCase}Query
    ├── dto/            # DTO：{Entity}DTO
    └── *AppService.java # 应用服务
```

## 关键查找

| 目标   | 位置                             |
|------|--------------------------------|
| 应用服务 | `app/**/*AppService.java`      |
| 命令对象 | `app/**/command/*Command.java` |
| 查询对象 | `app/**/query/*Query.java`     |
| DTO  | `app/**/dto/*DTO.java`         |

## 核心规则

### CQRS 模式（NON-NEGOTIABLE）

| 规则     | 说明                             |
|--------|--------------------------------|
| 命令查询分离 | Command 和 Query 必须严格分离         |
| 命令操作   | 修改状态，返回 void/ID，**禁止返回完整领域模型** |
| 查询操作   | 只读操作，返回 DTO，**不得包含业务逻辑**       |
| 事务边界   | 命令操作必须在事务边界内执行                 |

```java
public class OrderAppService {
    // 命令：创建订单
    @Transactional(rollbackFor = Exception.class)
    public OrderId create(CreateOrderCommand cmd) {
        OrderAggr order = OrderAggr.create(cmd.getCustomerId(), cmd.getTotalAmount());
        orderRepository.save(order);
        return order.getId();
    }
    
    // 查询：查询订单
    @Transactional(readOnly = true)
    public List<OrderDTO> query(OrderQuery query) {
        return orderRepository.findByCustomerId(query.getCustomerId())
                       .stream().map(this::toDTO).toList();
    }
}
```

### 事务管理（NON-NEGOTIABLE）

| 规则   | 说明                                                   |
|------|------------------------------------------------------|
| 唯一位置 | 必须位于 AppService 方法                                   |
| 修改操作 | 必须使用 `@Transactional(rollbackFor = Exception.class)` |
| 只读查询 | 建议使用 `@Transactional(readOnly = true)` 优化性能          |

### DTO 转换职责

| 层级             | 转换方向              |
|----------------|-------------------|
| Controller     | Request → Command |
| AppService     | Domain → DTO      |
| Infrastructure | Domain ↔ DO       |

## 禁止

| ❌ 禁止                        | ✅ 正确                |
|-----------------------------|---------------------|
| Domain 层使用 `@Transactional` | 事务边界在 AppService 方法 |
| 命令返回完整领域模型                  | 返回 ID 或值对象          |
| 查询包含业务逻辑                    | 查询仅做数据组装            |
| 大事务包含多聚合                    | 拆分为多个独立事务           |

---
← [项目知识库](../AGENTS.md) | **版本**: 3.3 | **更新**: 2026-02-19
