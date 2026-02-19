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

## 代码模板

### Request

```java

@Data
public class CreateOrderRequest {

    @NotBlank(message = "客户ID不能为空")
    private String customerId;

    @NotNull(message = "总金额不能为空")
    private BigDecimal totalAmount;

}
```

### Response

```java
@Getter @Builder(setterPrefix = "set")
public class OrderResponse {
    private String orderId;
    private String status;
    private BigDecimal totalAmount;
    private Instant createTime;
    
    // 从 DTO 转换
    public static OrderResponse fromDTO(OrderDTO dto) {
        return OrderResponse.builder()
            .setOrderId(dto.getOrderId())
            .setStatus(dto.getStatus())
            .setTotalAmount(dto.getTotalAmount())
            .setCreateTime(dto.getCreateTime())
            .build();
    }
}
```

### Controller

```java
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderAppService orderAppService;
    
    // 创建订单
    @PostMapping
    @BusinessLog
    public BaseResult<String> create(@RequestBody @Valid CreateOrderRequest request) {
        CreateOrderCommand command = toCommand(request);
        String orderId = orderAppService.create(command);
        return BaseResult.success(orderId);
    }
    
    // 取消订单（状态变更）
    @PostMapping("/{id}/cancel")
    @BusinessLog
    public BaseResult<Void> cancel(@PathVariable String id) {
        orderAppService.cancel(id);
        return BaseResult.success();
    }
    
    // 查询单条
    @GetMapping("/{id}")
    public BaseResult<OrderResponse> getById(@PathVariable String id) {
        OrderDTO dto = orderAppService.getById(id);
        return BaseResult.success(OrderResponse.fromDTO(dto));
    }
    
    // 查询列表
    @GetMapping
    public BaseResult<List<OrderResponse>> list(OrderQueryRequest request) {
        OrderQuery query = toQuery(request);
        List<OrderDTO> list = orderAppService.query(query);
        List<OrderResponse> response = list.stream()
            .map(OrderResponse::fromDTO)
            .toList();
        return BaseResult.success(response);
    }
    
    // Request → Command
    private CreateOrderCommand toCommand(CreateOrderRequest req) {
        return CreateOrderCommand.builder()
            .customerId(req.getCustomerId())
            .totalAmount(req.getTotalAmount())
            .build();
    }
}
```

### 事件监听器

```java
@Slf4j
@RequiredArgsConstructor
public class SpringDomainEventListener {
    
    private final EventDispatcher eventDispatcher;
    
    @EventListener
    @Async("virtualTaskExecutor")
    public void onEvent(Event<?> event) {
        log.debug("接收事件: eventId={}", event.getEid());
        
        // 忽略非 DOMAIN 源事件
        if (event.getType().getSource() != Source.DOMAIN) {
            return;
        }
        
        // 委托给 EventDispatcher 处理
        eventDispatcher.dispatch(event, false);
    }
}
```

## 禁止

| ❌ 禁止                  | ✅ 正确                   |
|-----------------------|------------------------|
| Adapter 层创建配置类        | 配置类在 start 模块 config 包 |
| Controller 包含业务逻辑     | 业务逻辑在 Domain 层         |
| 返回完整领域模型              | 返回 Response DTO        |
| 直接依赖 Infrastructure 层 | 通过 Application 层间接访问   |

---
← [项目知识库](../AGENTS.md) | **版本**: 3.4 | **更新**: 2026-02-19
