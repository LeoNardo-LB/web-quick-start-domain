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

| 目标   | 位置                                 |
|------|------------------------------------|
| 聚合根  | `{模块}/model/*Aggr.java`            |
| 实体   | `{模块}/model/*.java`（继承 Entity）     |
| 值对象  | `{模块}/model/valueobject/*.java`    |
| 仓储接口 | `{模块}/repository/*Repository.java` |
| 领域服务 | `{模块}/service/*DomainService.java` |
| 领域枚举 | 内嵌于实体类或 `enums/` 目录                |

## 核心规则

### 纯净性约束（NON-NEGOTIABLE）

| 规则        | 说明                                              |
|-----------|-------------------------------------------------|
| 依赖方向      | Adapter → Application → Domain ← Infrastructure |
| Domain 纯净 | **零外部依赖**，仅包含纯业务逻辑                              |
| 聚合根       | 唯一允许通过 Repository 持久化的领域对象                      |
| 值对象       | 必须不可变，通过属性值判断相等性                                |

**禁止引入**：Spring 框架、`@Transactional`、`@Autowired`、Infrastructure 实现类

## 代码模板

### 聚合根

```java

@Getter
@SuperBuilder(setterPrefix = "set")
public class OrderAggr extends AggregateRoot {
    private OrderId id;
    private OrderStatus status;
    
    // 静态工厂创建
    public static OrderAggr create(String customerId) {
        OrderAggr order = new OrderAggr();
        order.id = OrderId.generate();
        order.status = OrderStatus.CREATED;
        order.markAsCreated();
        order.recordEvent(new OrderCreatedEvent(order.id));
        return order;
    }

    // 业务方法：验证 → 修改 → 更新时间戳 → 发布事件
    public void pay() {
        if (!status.canPay()) throw new BizException("状态不允许支付");
        this.status = OrderStatus.PAID;
        this.markAsUpdated();
        this.recordEvent(new OrderPaidEvent(this.id));
    }
}
```

### 实体（参考 FileMetadata）

```java

@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
public class FileMetadata extends Entity {

    private String fileName;
    private String filePath;
    private String fileUrl;
    private Long   fileSize;
    private Status status;

    // 业务判断方法
    public boolean isActive() {return status == Status.ACTIVE;}

    public boolean isImage() {return contentType != null && contentType.startsWith("image/");}

    // 状态变更方法
    public void markAsDeleted() {
        this.status = Status.DELETED;
        this.markAsUpdated();  // 继承自 Entity
    }

    // 内嵌枚举
    @Getter
    public enum Status {
        ACTIVE("有效"),
        DELETED("已删除");
        private final String desc;

        Status(String desc) {this.desc = desc;}
    }

}
```

### 仓储接口

```java
public interface FileRepository {

    // 保存（新增/更新）
    FileBusiness save(FileBusiness fileBusiness);

    // 单条查询
    Optional<FileBusiness> findById(String id);

    // 列表查询
    List<FileBusiness> findByBusinessIdAndTypeAndUsage(String businessId, Type type, Usage usage);

    // 删除
    void deleteById(String id);

}
```

### 领域服务

```java
public interface FileDomainService {

    // 上传文件（协调多个领域对象）
    void uploadFile(InputStream inputStream, FileMetadata metadata, FileBusiness business);

    // 查询
    List<FileBusiness> listFileBusinesss(String businessId, Type type, Usage usage);

    FileBusiness getFileBusiness(String id);

    // 获取文件URL（协调 OssClient）
    String getFileUrl(String fileMetaId);

}
```

### 枚举模式

**内嵌枚举（推荐）**：

```java

@Getter
public enum Status {
    ACTIVE("有效"),
    DELETED("已删除");
    private final String desc;

    Status(String desc) {this.desc = desc;}
}
```

**状态判断方法**：

```java
public enum OrderStatus {
    CREATED {
        boolean canPay() {return true;}
    },
    PAID {
        boolean canPay() {return false;}
    };

    abstract boolean canPay();
}
```

**双向映射（需要 code 字段时）**：

```java

@Getter
public enum OrderType {
    NORMAL("normal", "普通订单"),
    VIP("vip", "VIP订单");

    private final String code;
    private final String desc;

    OrderType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 双向映射
    public static OrderType fromCode(String code) {
        for (OrderType t : values()) {
            if (t.code.equals(code))
                return t;
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
```

## 禁止

| ❌ 禁止                | ✅ 正确                                       |
|---------------------|--------------------------------------------|
| 使用 `@Transactional` | 事务边界在 Application 层                        |
| 使用 `@Data` 注解       | 使用 `@Builder` + `@RequiredArgsConstructor` |
| 跨聚合根直接调用方法          | 通过领域事件异步处理                                 |
| 字段使用 String 存储枚举值   | 使用枚举类型                                     |

---
← [项目知识库](../AGENTS.md) | **版本**: 3.4 | **更新**: 2026-02-19
