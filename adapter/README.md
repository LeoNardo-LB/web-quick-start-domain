# Adapter模块 README

## 1. 模块概述

Adapter（接口层/适配器层）是DDD四层架构的最外层，负责接收外部请求和返回响应。

### 核心理念

- **保持简洁**：Controller应该很薄，只负责HTTP处理，不包含业务逻辑
- **统一返回格式**：所有接口使用统一的返回格式（BaseResult）
- **全局异常处理**：统一处理异常，转换为友好的错误信息
- **参数验证**：使用JSR-303验证注解进行参数验证

### 关键特点

- HTTP请求处理（Controller）
- 参数验证（@Valid）
- 调用应用服务
- 返回统一格式（BaseResult）
- 事件监听（EventListener）
- 定时任务（Schedule）

### 架构定位

```
┌─────────────────────────────────────────┐
│         Adapter (接口层) ★ 本模块       │
│  ┌──────────────────────────────────┐   │
│  │  Controller (控制器)              │   │
│  │  Request/Response DTO (请求/响应) │   │
│  │  EventListener (事件监听器)       │   │
│  │  Schedule (定时任务)              │   │
│  │  GlobalExceptionHandler (异常处理) │   │
│  └──────────────────────────────────┘   │
├─────────────────────────────────────────┤
│         Application (应用层)             │
├─────────────────────────────────────────┤
│          Domain (领域层)                 │
├─────────────────────────────────────────┤
│      Infrastructure (基础设施层)         │
└─────────────────────────────────────────┘
```

---

## 2. 目录结构

```
adapter/
├── src/main/java/{groupId}/adapter/
│   ├── access/                       # 访问层（核心）
│   │   └── web/
│   │       └── config/               # Web配置
│   │           └── WebExceptionAdvise.java # 全局异常处理
│   │
│   ├── _shared/                      # 共享组件
│   │   ├── result/                   # 统一返回格式
│   │   │   └── BaseResult.java       # 统一返回结果
│   │   └── enums/                    # 枚举
│   │       └── ResultEnum.java       # 返回码枚举
│   │
│   └── _example/                     # 示例业务（订单模块）
│       └── order/
│           ├── web/                  # Web层
│           │   ├── api/              # Controller
│           │   │   └── OrderController.java
│           │   └── dto/              # DTO
│           │       ├── request/      # 请求DTO
│           │       │   ├── CreateOrderRequest.java
│           │       │   ├── PayOrderRequest.java
│           │       │   └── CancelOrderRequest.java
│           │       └── response/     # 响应DTO
│           │           └── OrderResponse.java
│           └── listener/             # 事件监听器
│               └── OrderEventHandler.java
│
└── README.md                          # 本文档
```

### 包结构说明

- **access/**：访问层核心配置，如全局异常处理、拦截器等
- **_shared/**：共享组件，所有业务模块通用
- **_example/**：示例业务模块，演示接口层开发实践
- **web/**：Web层，包含Controller和DTO
- **listener/**：事件监听器，处理领域事件

---

## 3. 核心职责与边界

### 3.1 核心职责

**HTTP请求处理**

- 接收HTTP请求
- 参数验证（@Valid）
- 调用应用服务
- 返回响应

**事件监听**

- 监听领域事件
- 处理事件（如发送通知、记录日志）
- 解耦业务流程

**定时任务**

- 定时执行任务
- 调用应用服务
- 处理批量数据

**全局异常处理**

- 捕获异常
- 转换为友好的错误信息
- 返回统一格式

### 3.2 能力边界

**✅ Adapter层能做什么**

- HTTP请求处理（接收请求、返回响应）
- 参数验证（JSR-303注解）
- 调用应用服务
- DTO转换（Request → Command, DTO → Response）
- 异常处理和错误信息转换
- 事件监听和处理
- 定时任务调度

**❌ Adapter层不能做什么**

- 业务逻辑（业务逻辑在Domain层）
- 用例编排（用例编排在Application层）
- 直接操作数据库（通过Application层间接操作）
- 调用外部系统（通过Domain层的端口接口）
- 事务管理（事务在Application层）

---

## 4. 关键组件类型

### 4.1 Controller（控制器）

**作用**

- 接收HTTP请求
- 参数验证
- 调用应用服务
- 返回统一格式响应

**职责**

- 处理HTTP请求和响应
- 参数验证
- DTO转换
- 异常处理

**编写要点**

1. 使用`@RestController`注解
2. 使用`@RequestMapping`定义路由前缀
3. 构造器注入依赖
4. 使用`@Valid`触发参数验证
5. 返回BaseResult统一格式

**伪代码示例**

```java
// Controller基本结构
@RestController
@RequestMapping("/api/xxx")
public class XxxController {

    private final XxxAppService appService;

    // 构造器注入
    public XxxController(XxxAppService appService) {
        this.appService = appService;
    }

    // POST: 创建资源
    @PostMapping
    public BaseResult<XxxDTO> create(
        @Valid @RequestBody CreateXxxRequest request
    ) {
        // 1. Request → Command
        CreateXxxCommand command = CreateXxxCommand.from(request);

        // 2. 调用应用服务
        XxxDTO result = appService.createXxx(command);

        // 3. DTO → Response
        XxxResponse response = XxxResponse.from(result);

        // 4. 统一返回
        return BaseResult.success(response);
    }

    // GET: 查询资源
    @GetMapping("/{id}")
    public BaseResult<XxxDTO> getById(@PathVariable String id) {
        GetXxxQuery query = new GetXxxQuery(id);
        XxxDTO result = appService.getXxx(query);
        return BaseResult.success(result);
    }

    // PUT: 更新资源
    @PutMapping("/{id}")
    public BaseResult<XxxDTO> update(
        @PathVariable String id,
        @Valid @RequestBody UpdateXxxRequest request
    ) {
        UpdateXxxCommand command = UpdateXxxCommand.from(id, request);
        XxxDTO result = appService.updateXxx(command);
        return BaseResult.success(result);
    }

    // DELETE: 删除资源
    @DeleteMapping("/{id}")
    public BaseResult<Void> delete(@PathVariable String id) {
        appService.deleteXxx(id);
        return BaseResult.success();
    }
}
```

**与其他组件协作**

- 接收HTTP请求
- 转换Request为Command
- 调用ApplicationService
- 转换DTO为Response
- 返回BaseResult

**边界**

- **保持简洁**：只处理HTTP，不做业务逻辑
- **不允许**：直接访问数据库、调用外部API、编写业务规则

---

### 4.2 Request DTO

**作用**

- 封装HTTP请求参数
- 提供参数验证
- 表达请求意图

**职责**

- 携带请求参数
- 定义验证规则
- 转换为Command

**编写要点**

1. 使用Lombok的`@Getter`和`@Setter`
2. 使用JSR-303验证注解（@NotNull、@NotBlank、@Min、@Size等）
3. 嵌套对象使用@Valid进行级联验证
4. 提供`toCommand()`方法转换为Command

**常用验证注解**

- `@NotNull`：不能为null
- `@NotBlank`：字符串不能为空
- `@NotEmpty`：集合不能为空
- `@Size`：长度范围
- `@Min`、`@Max`：数值范围
- `@Email`：邮箱格式
- `@Pattern`：正则表达式

**伪代码示例**

```java
@Getter
@Setter
public class CreateXxxRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100")
    private String name;

    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    // 嵌套对象
    @Valid
    @NotNull
    private XxxItemRequest item;

    // 转换为Command
    public CreateXxxCommand toCommand() {
        return CreateXxxCommand.builder()
            .name(this.name)
            .quantity(this.quantity)
            .item(this.item.toCommand())
            .build();
    }

    // 嵌套DTO
    @Getter
    @Setter
    public static class XxxItemRequest {
        @NotBlank(message = "XXX不能为空")
        private String xxx;

        @Min(value = 0, message = "数量不能小于0")
        private Integer quantity;
    }
}
```

**边界**

- 只用于HTTP请求
- 包含验证注解
- 不包含业务逻辑

---

### 4.3 Response DTO

**作用**

- 封装HTTP响应数据
- 定制返回数据结构
- 隐藏内部实现

**职责**

- 携带响应数据
- 提供计算字段
- 从DTO转换

**编写要点**

1. 使用Lombok的`@Getter`和`@Setter`
2. 提供静态工厂方法`from(DTO)`
3. 可以包含计算字段
4. 不包含业务逻辑

**伪代码示例**

```java
@Getter
@Setter
public class XxxResponse {

    // 基本字段
    private String id;
    private String name;
    private String status;

    // 计算字段（不在DTO中）
    private String statusText;
    private String displayInfo;

    // 从DTO转换
    public static XxxResponse from(XxxDTO dto) {
        XxxResponse response = new XxxResponse();
        response.setId(dto.getId());
        response.setName(dto.getName());
        response.setStatus(dto.getStatus());

        // 计算字段
        response.setStatusText(formatStatus(dto.getStatus()));
        response.setDisplayInfo(dto.getName() + "(" + formatStatus(dto.getStatus()) + ")");

        return response;
    }

    // 格式化方法
    private static String formatStatus(String status) {
        // 格式化逻辑
        return status.toLowerCase();
    }

    // 批量转换
    public static List<XxxResponse> fromList(List<XxxDTO> dtoList) {
        return dtoList.stream()
            .map(XxxResponse::from)
            .collect(Collectors.toList());
    }
}
```

**边界**

- 只用于HTTP响应
- 不包含验证
- 不包含业务逻辑

---

### 4.4 统一返回格式（BaseResult）

**作用**

- 统一API响应结构
- 便于前端处理
- 携带元数据（时间、traceId）

**基本结构**

```java
{
    "code": 200,        // 业务状态码
    "message": "成功",   // 提示信息
    "data": {...},      // 业务数据
    "timestamp": 1234567890,
    "traceId": "trace-id-123"
}
```

**伪代码示例**

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResult<T> {

    private int code;           // 业务状态码
    private String message;      // 提示信息
    private T data;             // 业务数据
    private LocalDateTime time; // 时间戳
    private String traceId;     // 追踪ID

    // 成功返回（无数据）
    public static <T> BaseResult<T> success() {
        return new BaseResult<>(200, "success", null, LocalDateTime.now(), null);
    }

    // 成功返回（带数据）
    public static <T> BaseResult<T> success(T data) {
        return new BaseResult<>(200, "success", data, LocalDateTime.now(), null);
    }

    // 错误返回
    public static <T> BaseResult<T> error(int code, String message) {
        return new BaseResult<>(code, message, null, LocalDateTime.now(), null);
    }

    // 业务错误
    public static <T> BaseResult<T> bizError(String message) {
        return error(400, message);
    }

    // 系统错误
    public static <T> BaseResult<T> sysError(String message) {
        return error(500, message);
    }
}
```

**使用方式**

```java
// 成功返回
return BaseResult.success(data);
return BaseResult.success();

// 错误返回
return BaseResult.error(400, "参数错误");
return BaseResult.bizError("业务异常");
return BaseResult.sysError("系统异常");
```

**边界**

- 只用于包装响应
- 不包含业务逻辑
- 不修改数据

---

### 4.5 全局异常处理

**作用**

- 统一处理异常
- 转换为友好错误信息
- 记录日志

**职责**

- 捕获Controller抛出的异常
- 转换为BaseResult格式
- 记录错误日志

**编写要点**

1. 使用`@RestControllerAdvice`注解
2. 使用`@ExceptionHandler`定义异常处理方法
3. 按异常类型分别处理
4. 返回BaseResult格式

**伪代码示例**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public BaseResult<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return BaseResult.bizError(e.getMessage());
    }

    // 参数验证异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult<Void> handleValidationException(
        MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                          .getFieldErrors()
                          .stream()
                          .map(FieldError::getDefaultMessage)
                          .collect(Collectors.joining(", "));
        return BaseResult.error(400, message);
    }

    // 系统异常
    @ExceptionHandler(Exception.class)
    public BaseResult<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return BaseResult.sysError("系统异常，请联系管理员");
    }
}
```

**边界**

- 只处理HTTP层异常
- 不修改业务逻辑
- 不抛出异常

---

### 4.6 事件监听器（EventListener）

**作用**

- 监听领域事件
- 执行副作用（发送通知、同步数据等）
- 解耦业务流程

**职责**

- 监听特定领域事件
- 执行副作用操作
- 不影响主流程

**编写要点**

1. 实现`EventHandler<T>`接口
2. 使用`@EventListener`注解（可选）
3. 不抛出异常影响主流程
4. 保证幂等性

**伪代码示例**

```java
@Component
public class XxxEventListener implements EventHandler<XxxCreatedEvent> {

    private final NotificationService notificationService;

    public XxxEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void handle(XxxCreatedEvent event) {
        log.info("处理Xxx创建事件: {}", event.getAggregateId());

        try {
            // 执行副作用：发送通知、同步数据等
            notificationService.sendNotification(...)

            log.info("Xxx创建事件处理完成: {}", event.getAggregateId());

        } catch (Exception e) {
            log.error("处理Xxx创建事件失败: {}", event.getAggregateId(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof XxxCreatedEvent;
    }
}
```

**边界**

- 不抛出异常影响主流程
- 可以异步执行
- 幂等性处理

---

## 5. 设计模式和原则

### 5.1 核心设计模式

**控制器模式**

- Controller处理HTTP请求
- 统一返回格式
- 全局异常处理

**DTO模式**

- Request接收参数
- Response返回数据
- 隐藏内部实现

**观察者模式**

- EventListener监听事件
- 解耦业务流程
- 异步处理副作用

### 5.2 RESTful原则

**资源命名**

- 使用名词复数：`/api/orders`
- 层级关系：`/api/orders/{orderId}/items`

**HTTP方法**

- POST：创建资源
- GET：查询资源
- PUT：更新资源
- DELETE：删除资源

**状态码**

- 200：成功
- 400：参数错误
- 500：系统错误

---

## 6. 开发指南

### 6.1 创建新Controller

**步骤1：创建Request DTO**

```java
@Getter
@Setter
public class CreateXxxRequest {
    @NotBlank
    private String name;

    public CreateXxxCommand toCommand() {
        return CreateXxxCommand.builder()
            .name(this.name)
            .build();
    }
}
```

**步骤2：创建Response DTO**

```java
@Getter
@Setter
public class XxxResponse {
    private String id;
    private String name;

    public static XxxResponse from(XxxDTO dto) {
        XxxResponse response = new XxxResponse();
        response.setId(dto.getId());
        response.setName(dto.getName());
        return response;
    }
}
```

**步骤3：创建Controller**

```java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {

    @PostMapping
    public BaseResult<XxxResponse> create(@Valid @RequestBody CreateXxxRequest request) {
        CreateXxxCommand command = request.toCommand();
        XxxDTO dto = appService.createXxx(command);
        return BaseResult.success(XxxResponse.from(dto));
    }
}
```

**步骤4：在start模块配置Bean**

Controller不需要显式配置，Spring Boot会自动扫描。

---

## 7. 配置说明

Adapter层的配置类在start模块完成，详细配置说明见`start/README.md`（在start模块）。

### 配置原则

1. **配置类位置**：配置类放在start模块的`config`包下
2. **按技术命名**：配置类按技术命名（如`AdapterWebConfig`、`AdapterListenerConfig`）
3. **使用@Bean方法**：使用`@Configuration` + `@Bean`模式
4. **Controller自动扫描**：Controller不需要显式配置

---

## 8. 常见问题FAQ

### Q1: Request DTO和Command的区别？

**A**:

| 特征   | Request DTO  | Command              |
|------|--------------|----------------------|
| 层级   | Adapter层     | Application层         |
| 用途   | 接收HTTP请求参数   | 封装用例参数               |
| 验证注解 | 包含验证注解       | 不包含验证注解              |
| 来源   | Controller使用 | ApplicationService使用 |

**示例对比**：

```java
// Request DTO：包含验证注解
public class CreateXxxRequest {
    @NotBlank(message = "名称不能为空")
    private String name;
}

// Command：不包含验证注解
public class CreateXxxCommand {
    private String name;
}

// Controller中转换
CreateXxxCommand command = request.toCommand();
```

---

### Q2: 为什么Controller要保持简洁？

**A**: Controller应该保持简洁，原因如下：

1. **职责单一**：Controller只负责HTTP处理，不负责业务逻辑
2. **易于测试**：简洁的Controller更容易编写单元测试
3. **符合DDD原则**：业务逻辑在Domain层，用例编排在Application层
4. **可维护性**：避免Controller变得臃肿

---

### Q3: 全局异常处理如何工作？

**A**: 使用`@RestControllerAdvice`统一处理异常：

**工作流程**：

1. Controller抛出异常
2. `@RestControllerAdvice`捕获异常
3. 根据异常类型调用对应的处理方法
4. 转换为BaseResult格式
5. 返回错误响应

---

### Q4: 事件监听器在哪里配置？

**A**: 事件监听器在start模块配置为Bean：

```java
@Configuration
public class XxxConfigure {

    @Bean
    public EventHandler<?> xxxEventHandler(...) {
        return new XxxEventListener(...)
    }
}
```

---

### Q5: 如何处理参数验证失败？

**A**: 使用`@Valid`触发验证，全局异常处理器捕获：

```java
@PostMapping
public BaseResult<XxxResponse> create(@Valid @RequestBody CreateXxxRequest request) {
    // @Valid触发验证
    // 验证失败抛出MethodArgumentNotValidException
}

// 全局异常处理
@ExceptionHandler(MethodArgumentNotValidException.class)
public BaseResult<Void> handleValidationException(MethodArgumentNotValidException e) {
    String message = e.getBindingResult()
                      .getFieldErrors()
                      .stream()
                      .map(FieldError::getDefaultMessage)
                      .collect(Collectors.joining(", "));
    return BaseResult.error(400, message);
}
```

---

## 9. 相关文档

- `../README.md` - 项目根README.md（项目整体架构说明）
- `../_docs/specification/业务代码编写规范.md` - 业务代码编写规范（编码标准详细参考）
- `app/README.md` - 应用层开发指南（在 app 模块）
- `domain/README.md` - 领域层开发指南（在 domain 模块）
- `start/README.md` - 启动模块配置指南（在 start 模块）

---

**文档版本**: v2.0 (概念指导版)
**最后更新**: 2026-01-13
**维护者**: Leonardo
