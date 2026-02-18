# Adapter 层 - 接口适配层

**接口适配层**：REST 接口、事件监听、定时调度、异常处理、参数验证。

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

| 目标           | 位置                                           | 说明                      |
|--------------|----------------------------------------------|-------------------------|
| Controller   | `adapter/web/api/*Controller.java`           | REST 端点                 |
| Request DTO  | `adapter/web/dto/request/*Request.java`      | 命名 `{UseCase}Request`   |
| Response DTO | `adapter/web/dto/response/*Response.java`    | 命名 `{Entity}Response`   |
| 异常处理         | `adapter/web/config/WebExceptionAdvise.java` | `@RestControllerAdvice` |
| 事件监听         | `adapter/listener/`                          | Spring/Kafka 事件监听       |
| 定时任务         | `adapter/schedule/`                          | 事件重试调度                  |

## 核心规则

### Request/Response 设计（NON-NEGOTIABLE）

| 规则   | Request                                        | Response                         |
|------|------------------------------------------------|----------------------------------|
| 命名格式 | `{UseCase}Request`                             | `{Entity}Response`               |
| 验证   | JSR-303 注解（`@NotNull`、`@NotBlank`、`@NotEmpty`） | -                                |
| 构建模式 | -                                              | `@Builder(setterPrefix = "set")` |
| 转换方法 | -                                              | `fromDTO(DTO dto)` 静态方法          |
| 可见性  | **必须 `public`**（对外 API 契约）                     | **必须 `public`**                  |

```java
// Request 示例
@Data
public class CreateOrderRequest {
    @NotBlank(message = "客户ID不能为空")
    private String customerId;
    @NotNull(message = "金额不能为空")
    private MoneyDTO totalAmount;
}

// Response 示例
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

### 参数验证原则（NON-NEGOTIABLE）

| 字段类型 | 验证注解                 |
|------|----------------------|
| 字符串  | `@NotBlank`（非空非空白）   |
| 对象   | `@NotNull`（非 null）   |
| 集合   | `@NotEmpty`（非空）      |
| 自定义  | `@Validated` + 自定义注解 |

**验证消息必须国际化（支持多语言）。**

### 异常处理原则（NON-NEGOTIABLE）

**异常分类**：

| 异常类型              | HTTP 状态码 | 说明                 |
|-------------------|----------|--------------------|
| `BizException`    | 400      | 可预期的业务异常           |
| `ClientException` | 4xx      | 外部客户端调用失败（必须提供错误码） |
| `SysException`    | 500      | 系统内部异常             |

**统一异常处理**：

```java
@RestControllerAdvice
public class WebExceptionAdvise {
    @ExceptionHandler(BizException.class)
    public BaseResult<?> handleBizException(BizException e) {
        return BaseResult.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(SysException.class)
    public BaseResult<?> handleSysException(SysException e) {
        return BaseResult.error(ResultEnum.SYSTEM_ERROR);
    }
}
```

**BaseResult 统一封装**：`code`、`data`、`message`、`time`、`traceId`

**禁止**：

- ❌ 直接返回异常堆栈给前端
- ❌ 敏感信息未脱敏

### 事件监听规范

| 规则   | 说明                                   |
|------|--------------------------------------|
| 异步执行 | 必须使用 `@Async("virtualTaskExecutor")` |
| 事件来源 | 仅处理 `Source.DOMAIN` 源的事件             |
| 分发机制 | 通过 `EventDispatcher` 分发到具体 Handler   |

### API 设计原则

| 规则         | 说明                   |
|------------|----------------------|
| RESTful 命名 | 遵循 REST 规范，确保可读性和一致性 |
| 版本控制       | 提供版本控制机制，确保向后兼容      |
| 幂等性        | 关键操作必须保证幂等性          |
| 限流降级       | 实现限流和熔断机制            |

### 定时任务（事件重试）

**重试策略**：

1. **指数退避策略**（`ExponentialBackoffRetryStrategy`）：内置实现，简单场景
2. **外部调度策略**（`ExternalSchedulerRetryStrategy`）：XXL-JOB、PowerJob，分布式场景

```yaml
# application.yml
middleware:
  event:
    retry:
      strategy: external-scheduler
      interval-minutes: 5
```

## 反模式（禁止）

| ❌ 禁止              | ✅ 正确做法                             |
|-------------------|------------------------------------|
| Adapter 层创建配置类    | 配置类在 start 模块 config 包             |
| Controller 包含业务逻辑 | 业务逻辑在 Domain 层                     |
| 事件处理器同步执行         | 使用 `@Async("virtualTaskExecutor")` |
| 直接返回异常堆栈          | 统一异常处理，脱敏敏感信息                      |
| 返回完整领域模型          | 返回 Response DTO                    |

## 模块边界

### 对外暴露

| 类型            | 位置                                | 说明      |
|---------------|-----------------------------------|---------|
| Controller    | `web/api/*Controller.java`        | REST 端点 |
| Request       | `web/dto/request/*Request.java`   | 请求 DTO  |
| Response      | `web/dto/response/*Response.java` | 响应 DTO  |
| EventListener | `listener/*Listener.java`         | 事件监听器   |

### 依赖下游

| 模块             | 依赖方式   | 说明                   |
|----------------|--------|----------------------|
| Application    | 直接依赖   | 调用 AppService        |
| Domain         | 直接依赖   | 使用领域枚举、值对象           |
| Infrastructure | **禁止** | 通过 Application 层间接访问 |

### 禁止

- ❌ 直接依赖 Infrastructure 层
- ❌ Controller 包含业务逻辑
- ❌ 创建配置类（配置类在 start 模块）
- ❌ 返回完整领域模型（应返回 Response DTO）
- ❌ 事件处理器同步执行

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
