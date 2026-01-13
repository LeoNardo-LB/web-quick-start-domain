# App模块 README

## 1. 模块概述

Application（应用层）是DDD四层架构的编排层,负责用例流程编排和事务边界管理。

### 核心理念

- **薄薄的一层**：应用服务只做编排，不包含业务逻辑
- **事务边界**：定义事务的开始和结束
- **DTO转换**：领域对象与外部数据传输对象的转换
- **事件发布**：发布领域事件，触发后续流程

### 关键特点

- 使用应用服务（ApplicationService）编排用例
- 使用命令对象（Command）封装写操作
- 使用查询对象（Query）封装读操作
- 使用DTO对象（DTO）跨层数据传输
- 实现CQRS模式（命令查询职责分离）

### 架构定位

```
┌─────────────────────────────────────────┐
│          Adapter (接口层)                │
├─────────────────────────────────────────┤
│         Application (应用层) ★ 本模块    │
│  ┌──────────────────────────────────┐   │
│  │  ApplicationService (应用服务)    │   │
│  │  Command (命令对象)               │   │
│  │  Query (查询对象)                 │   │
│  │  DTO (数据传输对象)               │   │
│  │  Configure (配置类)               │   │
│  └──────────────────────────────────┘   │
├─────────────────────────────────────────┤
│          Domain (领域层)                 │
├─────────────────────────────────────────┤
│      Infrastructure (基础设施层)         │
└─────────────────────────────────────────┘
```

---

## 2. 目录结构

```
app/
├── src/main/java/{groupId}/app/
│   ├── _shared/                    # 共享基础组件
│   │   ├── base/                   # 基类
│   │   │   └── BaseApplicationService.java
│   │   ├── command/                # 命令对象基类
│   │   ├── query/                  # 查询对象基类
│   │   └── dto/                    # DTO基类
│   │
│   ├── _example/                   # 示例业务（订单模块）
│   │   └── order/
│   │       ├── OrderAppService.java        # 订单应用服务
│   │       ├── command/
│   │       │   ├── CreateOrderCommand.java
│   │       │   ├── PayOrderCommand.java
│   │       │   └── CancelOrderCommand.java
│   │       ├── query/
│   │       │   ├── GetOrderQuery.java
│   │       │   └── ListOrderQuery.java
│   │       └── dto/
│   │           └── OrderDTO.java
│   │
│   └── common/                     # 通用业务组件
│       ├── file/                   # 文件管理
│       └── ...
│
└── README.md                        # 本文档
```

### 包结构说明

- **_shared/**：共享基础组件，所有应用服务通用的基类和接口
- **_example/**：示例业务模块，演示应用层实践（订单模块）
- **common/**：通用业务组件，可在实际项目中使用的应用服务

---

## 3. 核心职责与边界

### 3.1 核心职责

**用例编排**

- 编排领域对象的协作
- 定义用例的执行流程
- 协调多个聚合根的交互

**事务管理**

- 定义事务边界
- 确保事务的一致性
- 处理事务回滚

**DTO转换**

- 领域对象转换为DTO
- Command转换为领域对象参数
- 定制返回数据结构

**事件发布**

- 收集聚合根发布的领域事件
- 统一发布事件
- 确保事件发布的事务一致性

### 3.2 能力边界

**✅ App层能做什么**

- 编排用例流程
- 调用领域层的业务方法
- 定义事务边界（@Transactional）
- 转换DTO对象
- 发布领域事件
- 调用基础设施层服务

**❌ App层不能做什么**

- 包含业务逻辑（业务逻辑在Domain层）
- 直接访问数据库
- 直接调用外部API
- 处理HTTP请求（由Adapter层负责）
- 包含技术细节（如数据库映射）

---

## 4. 关键组件类型

### 4.1 应用服务（ApplicationService）

**作用**

- 编排用例流程
- 定义事务边界
- 协调领域对象和基础设施

**职责**

- 接收Command或Query
- 调用领域层
- 持久化聚合根
- 发布领域事件
- 转换并返回DTO

**编写要点**

1. 继承`BaseApplicationService`（可选）
2. 构造器注入依赖
3. 命令方法使用`@Transactional`
4. 查询方法不使用事务
5. 保持简洁，只做编排

**伪代码示例**

```java
// 应用服务基本结构
public class XxxAppService {

    private final XxxAggrRepository repository;
    private final XxxDomainService domainService;
    private final EventPublisher eventPublisher;

    // 构造器注入
    public XxxAppService(XxxAggrRepository repository,
                        XxxDomainService domainService,
                        EventPublisher eventPublisher) {
        this.repository = repository;
        this.domainService = domainService;
        this.eventPublisher = eventPublisher;
    }

    // 命令方法：修改状态
    @Transactional
    public XxxDTO createXxx(CreateXxxCommand command) {
        // 1. 参数验证（通过Command的@Valid注解）

        // 2. 调用领域层
        XxxAggr aggr = XxxAggr.create(command.get参数());

        // 3. 持久化
        repository.save(aggr);

        // 4. 发布领域事件
        aggr.getUncommittedEvents()
            .forEach(eventPublisher::publish);
        aggr.clearDomainEvents();

        // 5. 转换DTO并返回
        return XxxDTO.from(aggr);
    }

    // 查询方法：只读
    public XxxDTO getXxx(GetXxxQuery query) {
        // 1. 查询
        XxxAggr aggr = repository.findById(query.getId())
                                 .orElseThrow(()->new NotFoundException());

        // 2. 转换DTO
        return XxxDTO.from(aggr);
    }
}
```

**与其他组件协作**

- 被`Controller`调用
- 调用`DomainService`和`Repository`
- 发布事件给`EventListener`
- 转换DTO返回给Controller

**边界**

- **必须薄**：只做编排，不包含业务逻辑
- **事务边界**：一个方法一个事务
- **不允许**：直接访问数据库、调用外部API、编写业务规则

---

### 4.2 命令对象（Command）

**作用**

- 封装写操作的输入参数
- 提供参数验证
- 表达业务意图

**职责**

- 携带写操作所需参数
- 定义验证规则
- 转换为领域对象参数

**编写要点**

1. 使用Lombok的`@Getter`（不要用@Data）
2. 使用JSR-303验证注解（@NotNull、@Min、@Size等）
3. 提供`toXxxParams()`方法转换为领域对象参数
4. 嵌套对象使用@Valid进行级联验证

**伪代码示例**

```java
@Getter
@AllArgsConstructor
public class CreateXxxCommand {

    @NotNull(message = "XXX不能为空")
    private String xxx;

    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    // 嵌套对象验证
    @Valid
    @NotNull
    private XxxNestedObject nested;

    // 转换为领域对象参数
    public XxxCreateParams toParams() {
        return XxxCreateParams.builder()
            .xxx(this.xxx)
            .quantity(this.quantity)
            .remark(this.remark)
            .nested(this.nested.toParams())
            .build();
    }
}
```

**使用场景**

- 创建资源
- 修改资源
- 删除资源
- 状态转换

**边界**

- 只用于写操作
- 不包含业务逻辑
- 验证失败抛出ConstraintViolationException

---

### 4.3 查询对象（Query）

**作用**

- 封装读操作的查询条件
- 支持分页
- 表达查询意图

**职责**

- 携带查询所需参数
- 定义分页参数
- 支持排序

**编写要点**

1. 使用Lombok的`@Getter`和`@Setter`
2. 提供合理的默认值
3. 支持分页参数
4. 不需要验证注解（查询失败不抛异常）

**伪代码示例**

```java
@Getter
@Setter
public class GetXxxPageQuery {

    // 查询条件
    private String keyword;

    private XxxStatus status;

    // 分页参数
    private final Integer pageNum = 1;

    private final Integer pageSize = 10;

    // 排序参数
    private final String sortBy = "createTime";

    private final String sortOrder = "DESC";

    // 计算偏移量
    public long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }
}
```

**使用场景**

- 查询单个对象
- 查询列表
- 分页查询
- 统计查询

**边界**

- 只用于读操作
- 不修改状态
- 查询失败返回空结果

---

### 4.4 DTO对象（DTO）

**作用**

- 跨层数据传输
- 隐藏内部实现
- 定制返回数据

**职责**

- 携带返回数据
- 定制数据结构
- 提供计算字段

**编写要点**

1. 使用Lombok的`@Getter`和`@Setter`
2. 提供静态工厂方法`from(领域对象)`
3. 可以包含计算字段（如状态文本）
4. 不包含业务逻辑

**伪代码示例**

```java
@Getter
@Setter
public class XxxDTO {

    // 基本字段
    private String id;
    private String name;
    private String status;

    // 计算字段（不在领域对象中）
    private String statusText;
    private String displayInfo;

    // 从领域对象转换
    public static XxxDTO from(XxxAggr aggr) {
        XxxDTO dto = new XxxDTO();
        dto.setId(aggr.getId().getValue());
        dto.setName(aggr.getName());
        dto.setStatus(aggr.getStatus().name());

        // 计算字段
        dto.setStatusText(aggr.getStatus().getDescription());
        dto.setDisplayInfo(aggr.getName() + "(" + aggr.getStatus().getDescription() + ")");

        return dto;
    }

    // 批量转换
    public static List<XxxDTO> fromList(List<XxxAggr> aggrList) {
        return aggrList.stream()
            .map(XxxDTO::from)
            .collect(Collectors.toList());
    }
}
```

**使用场景**

- Controller返回数据
- 跨层数据传输
- API响应

**边界**

- 不包含业务逻辑
- 不修改领域对象
- 可以定制字段

---

### 4.5 CQRS模式

**概念**

- Command（命令）：写操作，修改状态
- Query（查询）：读操作，查询状态

**职责分离**

**命令侧**：

- 修改状态
- 使用Command对象
- 使用事务
- 返回执行结果

**查询侧**：

- 查询状态
- 使用Query对象
- 不使用事务
- 返回DTO

**伪代码示例**

```java
// 命令服务
public class XxxCommandService {

    @Transactional
    public XxxDTO createXxx(CreateXxxCommand command) {
        // 1. 调用领域层
        XxxAggr aggr = XxxAggr.create(command.toParams());

        // 2. 持久化
        repository.save(aggr);

        // 3. 发布事件
        publishEvents(aggr);

        // 4. 返回DTO
        return XxxDTO.from(aggr);
    }
}

// 查询服务
public class XxxQueryService {

    public XxxDTO getXxx(GetXxxQuery query) {
        // 1. 查询（不修改状态）
        XxxAggr aggr = repository.findById(query.getId())
                                 .orElseThrow(NotFoundException::new);

        // 2. 返回DTO
        return XxxDTO.from(aggr);
    }

    public PageResult<XxxDTO> listXxx(GetXxxPageQuery query) {
        // 1. 查询列表
        List<XxxAggr> aggrList = repository.findByCondition(query);

        // 2. 转换DTO
        List<XxxDTO> dtoList = XxxDTO.fromList(aggrList);

        // 3. 返回分页结果
        return PageResult.of(dtoList, query.getPageNum(), query.getPageSize());
    }
}
```

**优势**

- 职责清晰：读写分离
- 性能优化：查询可以优化（如使用缓存）
- 独立演进：读写模型可以独立变化

**边界**

- Command不返回数据（只返回结果）
- Query不修改状态
- 两者使用不同的DTO

---

## 5. 设计模式和原则

### 5.1 核心设计模式

**事务脚本模式**

- 应用服务编排用例流程
- 每个方法对应一个用例

**DTO模式**

- 使用DTO跨层数据传输
- 隐藏内部实现

**CQRS模式**

- 命令查询职责分离
- 读写使用不同的服务

### 5.2 DDD原则应用

**应用层是薄的一层**

- 只做编排，不包含业务逻辑
- 业务逻辑在领域层

**事务边界**

- 一个应用服务方法一个事务
- 事务在应用层开始和结束

**事件驱动**

- 聚合根发布事件
- 应用服务统一发布

---

## 6. 开发指南

### 6.1 创建新应用服务

**步骤1：确定用例**

- 识别用例的输入和输出
- 确定是命令还是查询
- 设计验证规则

**步骤2：创建Command或Query**

```java
// 命令对象
public class CreateXxxCommand {
    @NotNull
    private String xxx;

    public XxxCreateParams toParams() {
        // 转换逻辑
    }
}

// 查询对象
public class GetXxxQuery {
    private String id;
}
```

**步骤3：实现应用服务**

```java
public class XxxAppService {

    @Transactional
    public XxxDTO createXxx(CreateXxxCommand command) {
        // 编排逻辑
    }
}
```

**步骤4：在start模块配置Bean**

- 使用`@Configuration` + `@Bean`
- 配置ApplicationService的Bean

### 6.2 设计DTO

**什么时候需要DTO**

- 跨层数据传输
- 隐藏内部实现
- 定制返回数据

**设计原则**

- 只包含需要的字段
- 提供计算字段
- 不包含业务逻辑

### 6.3 实现CQRS

**分离读写**

- 命令服务：修改状态
- 查询服务：查询状态
- 使用不同的DTO

**优化查询**

- 查询可以使用缓存
- 查询可以使用独立的数据库
- 查询可以优化数据结构

---

## 7. 配置说明

应用服务的Bean配置在start模块完成，详细配置说明见[start/README.md](../start/README.md)。

### 配置原则

1. **配置类位置**：配置类放在start模块的`config`包下
2. **按业务模块命名**：配置类按业务模块命名（如`OrderConfigure`、`BlogConfigure`）
3. **使用@Bean方法**：使用`@Configuration` + `@Bean`模式

---

## 8. 常见问题FAQ

### Q1: 为什么应用服务要保持"薄"？

**A**: 应用服务保持"薄"的原因：

1. **职责单一**：应用服务只负责编排，业务逻辑在领域层
2. **易于测试**：薄的应用服务更容易单元测试
3. **防止业务逻辑泄漏**：避免业务逻辑分散在应用层
4. **符合DDD**：领域层是核心，应用层只是编排

**示例对比**：

```java
// ❌ 错误：应用服务包含业务逻辑
public XxxDTO createXxx(CreateXxxCommand command) {
    // 业务逻辑：验证状态
    if (command.getStatus() == Status.PAID) {
        throw new BusinessException("已支付状态不能创建");
    }

    // 业务逻辑：计算金额
    Money totalAmount = command.getUnitPrice()
                              .multiply(command.getQuantity());

    // 创建聚合根
    XxxAggr aggr = XxxAggr.create(...)
}

// ✅ 正确：业务逻辑在领域层
public XxxDTO createXxx(CreateXxxCommand command) {
    // 调用领域层（业务逻辑在聚合根中）
    XxxAggr aggr = XxxAggr.create(command.toParams());

    repository.save(aggr);
    return XxxDTO.from(aggr);
}
```

---

### Q2: Command和Query的区别？

**A**:

| 特征  | Command（命令）        | Query（查询） |
|-----|--------------------|-----------|
| 作用  | 修改状态               | 查询状态      |
| 参数  | 携带写操作所需参数          | 携带查询条件    |
| 验证  | 需要（@NotNull等）      | 不需要       |
| 事务  | 需要（@Transactional） | 不需要       |
| 返回值 | 执行结果               | 查询数据（DTO） |
| 副作用 | 有（修改状态、发布事件）       | 无         |

---

### Q3: DTO和领域对象的区别？

**A**:

| 特征   | 领域对象             | DTO（数据传输对象）           |
|------|------------------|-----------------------|
| 位置   | Domain层          | Application层、Adapter层 |
| 作用   | 封装业务逻辑           | 跨层数据传输                |
| 行为   | 包含业务方法           | 只包含getter/setter      |
| 验证   | 业务规则验证           | 参数验证（JSR-303）         |
| 生命周期 | 有（由Repository管理） | 无（临时对象）               |
| 示例   | OrderAggr        | OrderDTO              |

---

### Q4: 如何保证事务边界？

**A**: 通过以下机制保证事务边界：

1. **在应用服务方法上添加@Transactional**
2. **一个方法一个事务**
3. **事务在应用层开始和结束**

**示例**：

```java
@Transactional
public XxxDTO createXxx(CreateXxxCommand command) {
    // 事务开始
    XxxAggr aggr = XxxAggr.create(command.toParams());
    repository.save(aggr); // 持久化

    // 发布事件（事务提交后）
    aggr.getUncommittedEvents()
        .forEach(eventPublisher::publish);

    // 事务结束
    return XxxDTO.from(aggr);
}
```

---

### Q5: 什么时候需要创建单独的Command/Query服务？

**A**: 在以下情况下需要分离：

1. **查询复杂**：查询逻辑复杂，需要优化
2. **读写频率差异大**：读多写少
3. **性能要求高**：需要针对查询优化（如使用缓存）
4. **数据模型差异**：读写需要不同的数据结构

**一般情况下**：小型应用可以合并Command和Query在同一个AppService中。

---

## 9. 相关文档

- [项目根README.md](../README.md) - 项目整体架构说明
- [业务代码编写规范.md](../业务代码编写规范.md) - 编码标准详细参考
- [domain/README.md](../domain/README.md) - 领域层开发指南
- [adapter/README.md](../adapter/README.md) - 接口层开发指南
- [start/README.md](../start/README.md) - 启动模块配置指南

---

**文档版本**: v2.0 (概念指导版)
**最后更新**: 2026-01-13
**维护者**: Leonardo
