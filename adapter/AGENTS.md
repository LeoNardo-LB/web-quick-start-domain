# Adapter 层 - 接口适配层

**接口适配层**：REST 接口、事件监听、定时调度、异常处理。

## 目录结构

```
adapter/src/main/java/org/smm/archetype/adapter/
├── web/               # Web 层
│   ├── api/           # REST Controller
│   └── config/        # Web 配置（WebExceptionAdvise、Filter）
├── listener/          # 事件监听器（Spring/Kafka）
├── schedule/          # 定时任务（事件重试调度）
└── event/             # 事件分发（EventDispatcher、FailureHandler）
```

## 关键查找

| 目标           | 位置                                           |
|--------------|----------------------------------------------|
| Controller   | `adapter/web/api/*Controller.java`           |
| Request DTO  | `adapter/web/dto/request/*Request.java`      |
| Response DTO | `adapter/web/dto/response/*Response.java`    |
| 异常处理         | `adapter/web/config/WebExceptionAdvise.java` |
| 事件监听         | `adapter/listener/`                          |

## 核心规则

### Request/Response 设计（NON-NEGOTIABLE）

| 规则   | Request                            | Response                         |
|------|------------------------------------|----------------------------------|
| 命名格式 | `{UseCase}Request`                 | `{Entity}Response`               |
| 验证   | JSR-303 注解（`@NotNull`、`@NotBlank`） | -                                |
| 构建模式 | -                                  | `@Builder(setterPrefix = "set")` |
| 转换方法 | -                                  | `fromDTO(DTO dto)` 静态方法          |

```java
// Request
@Data
public class CreateOrderRequest {
    @NotBlank(message = "客户ID不能为空")
    private String customerId;
}

// Response
@Builder(setterPrefix = "set")
@Data
public class OrderResponse {
    private String orderId;
    private String status;
    
    public static OrderResponse fromDTO(OrderDTO dto) {
        return OrderResponse.builder()
            .setOrderId(dto.getOrderId())
            .setStatus(dto.getStatus().name())
            .build();
    }
}
```

### 异常处理（NON-NEGOTIABLE）

| 异常类型              | HTTP 状态码 |
|-------------------|----------|
| `BizException`    | 400      |
| `ClientException` | 4xx      |
| `SysException`    | 500      |

**BaseResult 统一封装**：`code`、`data`、`message`、`time`、`traceId`

### 事件监听规范

| 规则   | 说明                                   |
|------|--------------------------------------|
| 异步执行 | 必须使用 `@Async("virtualTaskExecutor")` |
| 分发机制 | 通过 `EventDispatcher` 分发到具体 Handler   |

### RESTful 端点规范

| 操作类型 | HTTP 方法 | 路径模式                            |
|------|---------|---------------------------------|
| 创建资源 | POST    | `/api/{resource}`               |
| 状态变更 | POST    | `/api/{resource}/{id}/{action}` |
| 查询单条 | GET     | `/api/{resource}/{id}`          |
| 查询列表 | GET     | `/api/{resource}`               |

## 禁止

| ❌ 禁止                  | ✅ 正确                   |
|-----------------------|------------------------|
| Adapter 层创建配置类        | 配置类在 start 模块 config 包 |
| Controller 包含业务逻辑     | 业务逻辑在 Domain 层         |
| 返回完整领域模型              | 返回 Response DTO        |
| 直接依赖 Infrastructure 层 | 通过 Application 层间接访问   |

---
← [项目知识库](../AGENTS.md) | **版本**: 3.3 | **更新**: 2026-02-19
