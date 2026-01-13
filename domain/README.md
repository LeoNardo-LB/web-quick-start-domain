# Domain模块 README

## 1. 模块概述

Domain（领域层）是DDD四层架构的核心层，负责封装核心业务逻辑和业务规则。

### 核心理念

- **纯净业务逻辑**：领域层不依赖任何其他层，只包含纯粹的业务逻辑
- **聚合根一致性边界**：通过聚合根（AggregateRoot）维护业务一致性
- **值对象不可变**：使用不可变的值对象（ValueObject）确保数据安全
- **业务方法封装**：通过业务方法而非setter修改状态

### 关键特点

- 使用聚合根（AggregateRoot）作为一致性边界
- 通过业务方法封装业务规则
- 使用领域事件（DomainEvent）实现解耦
- 定义仓储接口（Repository）而非实现
- 领域服务（DomainService）处理跨聚合根的业务逻辑

### 架构定位

```
┌─────────────────────────────────────────┐
│          Adapter (接口层)                │
├─────────────────────────────────────────┤
│         Application (应用层)             │
├─────────────────────────────────────────┤
│          Domain (领域层) ★ 本模块        │
│  ┌──────────────────────────────────┐   │
│  │  AggregateRoot (聚合根)           │   │
│  │  Entity (实体)                   │   │
│  │  ValueObject (值对象)             │   │
│  │  DomainService (领域服务)         │   │
│  │  Repository (仓储接口)            │   │
│  │  DomainEvent (领域事件)           │   │
│  └──────────────────────────────────┘   │
├─────────────────────────────────────────┤
│      Infrastructure (基础设施层)         │
└─────────────────────────────────────────┘
```

---

## 2. 目录结构

```
domain/
├── src/main/java/{groupId}/domain/
│   ├── _shared/                    # 共享基础组件
│   │   ├── base/                   # 基类
│   │   │   ├── AggregateRoot.java  # 聚合根基类
│   │   │   ├── Entity.java         # 实体基类
│   │   │   ├── ValueObject.java    # 值对象基类
│   │   │   ├── BaseRepository.java # 仓储接口基类
│   │   │   ├── DomainEvent.java    # 领域事件基类
│   │   │   └── ...
│   │   └── event/                  # 共享事件
│   │       └── EventPublisher.java # 事件发布器接口
│   │
│   ├── _example/                   # 示例业务（订单模块）
│   │   └── order/
│   │       ├── model/              # 领域模型
│   │       │   ├── OrderAggr.java  # 订单聚合根
│   │       │   ├── OrderItem.java  # 订单项实体
│   │       │   └── valueobject/    # 值对象
│   │       ├── repository/         # 仓储接口
│   │       │   └── OrderAggrRepository.java
│   │       └── service/            # 领域服务
│   │           └── OrderDomainService.java
│   │
│   └── common/                     # 通用业务组件
│       ├── file/                   # 文件管理
│       ├── notification/           # 通知服务
│       └── ...
│
└── README.md                        # 本文档
```

### 包结构说明

- **_shared/**：共享基础组件，所有业务模块通用的基类和接口
- **_example/**：示例业务模块，演示DDD建模实践（订单模块）
- **common/**：通用业务组件，可在实际项目中使用的业务对象

---

## 3. 核心职责与边界

### 3.1 核心职责

**领域建模**

- 定义聚合根（AggregateRoot）、实体（Entity）、值对象（ValueObject）
- 建立领域模型之间的关联关系
- 封装业务不变性约束

**业务规则封装**

- 通过聚合根的业务方法封装业务规则
- 确保聚合内的一致性边界
- 防止业务逻辑泄漏到其他层

**仓储接口定义**

- 定义聚合根的持久化接口
- 声明领域层的查询方法
- 隐藏数据访问细节

**领域事件定义**

- 定义领域事件
- 在聚合根中发布事件
- 实现跨聚合根的解耦

### 3.2 能力边界

**✅ Domain层能做什么**

- 定义领域模型（聚合根、实体、值对象）
- 封装业务逻辑和业务规则
- 定义仓储接口（Repository接口）
- 定义领域服务接口（DomainService）
- 发布领域事件（DomainEvent）
- 使用端口接口（如EmailService、SmsService）

**❌ Domain层不能做什么**

- 依赖其他层（Adapter、Application、Infrastructure）
- 使用Spring框架注解（@Component、@Service、@Repository等）
- 处理HTTP请求或响应
- 直接操作数据库或调用外部API
- 包含配置类（@Configuration）
- 处理事务管理（事务由Application层负责）
- 实现仓储接口（由Infrastructure层实现）
- 包含DTO对象（DTO属于Application层）

---

## 4. 关键组件类型

### 4.1 聚合根（AggregateRoot）

**作用**

- 作为一致性边界，维护内部实体的完整性
- 管理实体的生命周期
- 发布领域事件

**职责**

- 封装业务规则
- 保证内部状态的一致性
- 通过业务方法暴露行为，而非直接暴露内部状态

**编写要点**

1. 继承`AggregateRoot<T>`基类
2. 使用工厂方法创建（`create()`、`reconstitute()`）
3. 业务方法返回结果或抛出异常，不返回void
4. 通过`addDomainEvent()`发布事件

**伪代码示例**

```java
// 聚合根基本结构
public class XxxAggr extends AggregateRoot<XxxAggr> {

    // 1. 私有构造器，保证通过工厂方法创建
    private XxxAggr(Id id, ...) {
        this.id = id;
        this.status = Status.CREATED;
        // 初始化逻辑
    }

    // 2. 工厂方法：创建新聚合根
    public static XxxAggr create(参数列表) {
        // 业务规则验证
        if (参数不合法) {
            throw new BusinessException("错误原因");
        }

        // 创建聚合根
        XxxAggr aggr = new XxxAggr(id, ...)

        // 发布领域事件
        aggr.addDomainEvent(new XxxCreatedEvent(id));

        return aggr;
    }

    // 3. 工厂方法：从持久化重建
    public static XxxAggr reconstitute(参数列表) {
        return new XxxAggr(id, ...);
    }

    // 4. 业务方法：修改状态
    public Result businessMethod(参数) {
        // 前置条件检查
        if (状态不满足) {
            throw new BusinessException("错误原因");
        }

        // 执行业务逻辑
        修改内部状态;

        // 发布领域事件
        addDomainEvent(new XxxStateChangedEvent(id));

        return Result.success();
    }

    // 5. 只读查询方法
    public Xxx getXxx() {
        return UnmodifiableCollection(内部状态);
    }

```

**与其他组件协作**

- 被`ApplicationService`调用
- 通过`Repository`接口持久化
- 发布的事件被`EventListener`消费

**边界**

- 不依赖其他聚合根
- 不直接访问基础设施
- 不包含技术细节（如数据库映射）

---

### 4.2 实体（Entity）

**作用**

- 有唯一标识的对象
- 生命周期由聚合根管理
- 包含可变状态

**职责**

- 封装自身的行为
- 维护内部一致性
- 提供业务方法

**编写要点**

1. 继承`Entity<T>`基类
2. 必须有ID字段
3. 提供业务方法，不要暴露setter
4. 不可独立存在，必须属于某个聚合根

**伪代码示例**

```java
public class XxxItem extends Entity<XxxItem> {

    // 构造器由聚合根调用
    XxxItem(Id id, ...) {
        this.id = id;
        // 初始化
    }

    // 业务方法
    public void updateStatus(Status newStatus) {
        // 业务规则验证
        if (状态转换不合法) {
            throw new IllegalStateException("...");
        }
        this.status = newStatus;
        markAsUpdated(); // 标记为已更新
    }

    // 只读查询
    public Status getStatus() {
        return this.status;
    }
}
```

**边界**

- 不能独立持久化
- 不能发布领域事件
- 不能引用其他聚合根

---

### 4.3 值对象（ValueObject）

**作用**

- 描述性的、不可变的对象
- 通过值判断相等性，而非标识
- 替代基本类型的集合

**职责**

- 封装相关的属性
- 提供类型安全
- 保证不可变性

**编写要点**

1. 继承`ValueObject`基类
2. 所有字段`final`
3. 私有构造器，提供静态工厂方法
4. 重写`equalityFields()`方法
5. 不提供setter

**伪代码示例**

```java
@Getter
@AllArgsConstructor(access = PRIVATE)
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class Money extends ValueObject {

    private BigDecimal amount;
    private Currency currency;

    // 静态工厂方法
    public static Money of(BigDecimal amount, Currency currency) {
        // 验证逻辑
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        return new Money(amount, currency);
    }

    // 业务方法
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("币种不同");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    @Override
    protected List<Object> equalityFields() {
        return Arrays.asList(amount, currency);
    }
}
```

**使用场景**

- 代替基本类型（如用Money代替BigDecimal）
- 描述性概念（如Address、ContactInfo）
- 度量或描述性属性

**边界**

- 必须不可变
- 没有唯一标识
- 可以独立存在

---

### 4.4 仓储接口（Repository）

**作用**

- 定义聚合根的持久化抽象
- 作为领域层和基础设施层的桥梁
- 提供面向领域的查询语言

**职责**

- 声明持久化方法
- 定义查询契约
- 隐藏持久化细节

**编写要点**

1. 继承`BaseRepository<聚合根, ID>`
2. 接口放在domain层
3. 方法名反映业务意图，而非数据库操作
4. 返回领域对象，非DO对象

**伪代码示例**

```java
public interface XxxAggrRepository extends BaseRepository<XxxAggr, XxxId> {

    // 按业务标识查询
    Optional<XxxAggr> findByBusinessNo(String businessNo);

    // 复杂查询
    List<XxxAggr> findByCondition(XxxQuery query);

    // 分页查询
    PageResult<XxxAggr> queryPage(XxxQuery query);

    // 删除（软删除）
    void remove(XxxId id);
}
```

**与其他组件协作**

- 接口在domain层定义
- 实现在infrastructure层
- 被ApplicationService调用

**边界**

- 只定义接口，不实现
- 不依赖Spring注解
- 不暴露数据库细节

---

### 4.5 领域服务（DomainService）

**作用**

- 处理跨聚合根的业务逻辑
- 封装无状态的业务计算
- 协调多个聚合根的协作

**职责**

- 执行无法放在单个聚合根中的逻辑
- 提供领域相关的计算
- 协调聚合根之间的交互

**编写要点**

1. 只在无法放入聚合根时使用
2. 无状态，不依赖实例变量
3. 方法要有明确的业务含义

**伪代码示例**

```java
public interface XxxDomainService {

    // 跨聚合根的业务逻辑
    Result performXxx(XxxAggr aggr1, YyyAggr aggr2);

    // 复杂计算
    Money calculateXxx(List<XxxAggr> aggrs);

    // 验证规则
    void validateXxx(XxxRule rule);
}
```

**使用场景**

- 需要访问多个聚合根
- 需要调用外部服务
- 复杂的业务计算

**边界**

- 不处理持久化
- 不管理事务
- 不处理HTTP

---

## 5. 设计模式和原则

### 5.1 核心设计模式

**工厂模式**

- 使用静态工厂方法（`create()`、`reconstitute()`）创建聚合根
- 封装创建逻辑，保证聚合根的一致性

**仓储模式**

- 接口在领域层定义，实现在基础设施层
- 隐藏数据访问细节，提供面向领域的查询语言

**规格模式**

- 使用`Specification`封装业务规则
- 支持复杂的查询条件组合

### 5.2 DDD原则应用

**聚合根作为一致性边界**

- 外部只能持有聚合根的引用
- 修改聚合内对象必须通过聚合根的业务方法

**值对象不可变**

- 所有字段为final
- 运算方法返回新对象

**领域事件驱动**

- 聚合根发布事件
- 应用层消费事件

---

## 6. 开发指南

### 6.1 创建新聚合根

**步骤1：确定聚合根的边界**

- 识别业务核心概念
- 确定一致性要求
- 判断是否需要独立标识

**步骤2：定义聚合根类**

```java
public class XxxAggr extends AggregateRoot<XxxAggr> {
    // 字段定义
    // 工厂方法
    // 业务方法
}
```

**步骤3：定义业务方法**

- 每个业务方法对应一个业务操作
- 方法中验证业务规则
- 发布领域事件

**步骤4：创建Repository接口**

```java
public interface XxxAggrRepository extends BaseRepository<XxxAggr, XxxId> {
    // 查询方法
}
```

**步骤5：在start模块配置Bean**

- 使用`@Configuration` + `@Bean`
- 配置Repository实现的Bean

### 6.2 设计值对象

**什么时候使用值对象**

- 需要类型安全（如Money代替BigDecimal）
- 多个属性总是成组出现（如Address）
- 需要封装业务规则（如PhoneNumber）
- 对象是不可变的

**设计步骤**

1. 确定属性集合
2. 所有字段设为`final`
3. 提供私有构造器
4. 提供静态工厂方法
5. 重写`equalityFields()`方法

### 6.3 发布领域事件

**在聚合根中**

```java
public void businessMethod() {
    // 业务逻辑
    addDomainEvent(new XxxEvent(this.id));
}
```

**在应用服务中**

```java
@Transactional
public void executeXxx() {
    XxxAggr aggr = repository.findById(id);
    aggr.businessMethod(); // 添加事件
    repository.save(aggr);

    // 发布事件
    aggr.getUncommittedEvents().forEach(eventPublisher::publish);
    aggr.clearDomainEvents();
}
```

### 6.4 定义仓储接口

**接口定义规范**

- 接口名：`{聚合根名}Repository`
- 方法名：`findBy{业务属性}`
- 返回类型：`Optional<聚合根>`或`List<聚合根>`

**查询方法命名**

- 单个查询：`findById()`、`findByBusinessNo()`
- 列表查询：`findByCustomerId()`
- 分页查询：`queryPage()`
- 存在性检查：`existsByBusinessNo()`

---

## 7. 配置说明

Domain层本身不需要配置类，所有Bean由start模块统一配置。

### 配置原则

1. **配置类位置**：配置类放在start模块的`config`包下
2. **按聚合根命名**：配置类按聚合根命名（如`OrderConfigure`、`BlogConfigure`）
3. **使用@Bean方法**：使用`@Configuration` + `@Bean`模式，而非`@Service`等注解

### 配置示例

Domain层的Bean配置在start模块完成，详细配置说明见[start/README.md](../start/README.md)。

---

## 8. 常见问题FAQ

### Q1: 为什么聚合根没有setter方法？

**A**: 聚合根通过业务方法修改状态，而非直接setter，原因如下：

1. **封装业务规则**：业务方法可以包含状态验证、业务规则检查
2. **维护一致性**：确保状态转换的合法性（如只有已创建的订单才能支付）
3. **发布事件**：在业务方法中发布领域事件
4. **防止误用**：避免外部直接修改状态，破坏聚合内的一致性

**示例对比**：

```java
// ❌ 错误：使用setter
order.setStatus(OrderStatus.PAID); // 绕过业务规则

// ✅ 正确：使用业务方法
order.pay(paymentMethod, paidAmount); // 包含状态验证、金额校验、事件发布
```

---

### Q2: 什么时候使用领域服务？

**A**: 领域服务用于以下场景：

1. **跨聚合根的业务逻辑**：涉及多个聚合根的交互
2. **无状态的业务规则**：纯业务逻辑计算，不依赖聚合根状态
3. **端口接口**：调用外部服务（如库存服务、支付网关）

**判断标准**：

- 如果逻辑属于一个聚合根内部 → 放在聚合根的业务方法中
- 如果逻辑涉及多个聚合根 → 放在领域服务中
- 如果逻辑是无状态的计算 → 放在领域服务中

---

### Q3: 值对象和实体的区别？

**A**:

| 特征    | 值对象（ValueObject）    | 实体（Entity）             |
|-------|---------------------|------------------------|
| 唯一标识  | 无                   | 有（id字段）                |
| 可变性   | 不可变（Immutable）      | 可变                     |
| 相等性判断 | 基于属性值               | 基于唯一标识                 |
| 生命周期  | 依附于实体或聚合根           | 有独立生命周期                |
| 示例    | Money、Address、Email | OrderItem、FileBusiness |

---

### Q4: 领域事件什么时候发布？

**A**: 领域事件在聚合根的业务方法中添加（addDomainEvent），在应用服务中发布。

**完整流程**：

1. **Domain层**：在聚合根的业务方法中添加事件
2. **Application层**：在应用服务中发布事件
3. **Adapter层**：创建EventListener消费事件

---

### Q5: 如何保证聚合根的一致性？

**A**: 通过以下机制保证聚合根的一致性：

1. **聚合根作为一致性边界**
    - 外部只能持有聚合根的引用
    - 修改聚合内对象必须通过聚合根的业务方法

2. **业务方法封装**
    - 所有状态修改通过业务方法进行
    - 业务方法中验证业务规则

3. **事务边界**
    - 一次事务只修改一个聚合根
    - 聚合根保存时确保内部一致性

**示例**：

```java
// ❌ 错误：直接修改聚合内的对象
order.getItems().get(0).setQuantity(10); // 绕过聚合根

// ✅ 正确：通过聚合根的业务方法修改
order.updateItemQuantity(0, 10); // 通过聚合根，包含验证逻辑
```

---

## 9. 相关文档

- [项目根README.md](../README.md) - 项目整体架构说明
- [业务代码编写规范.md](../业务代码编写规范.md) - 编码标准详细参考
- [app/README.md](../app/README.md) - 应用层开发指南
- [infrastructure/README.md](../infrastructure/README.md) - 基础设施层开发指南
- [start/README.md](../start/README.md) - 启动模块配置指南

---

**文档版本**: v2.0 (概念指导版)
**最后更新**: 2026-01-13
**维护者**: Leonardo
