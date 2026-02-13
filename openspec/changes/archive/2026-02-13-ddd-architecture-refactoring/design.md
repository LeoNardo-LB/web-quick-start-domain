# DDD Architecture Refactoring - Technical Design

## Context

### 当前状态

项目是一个基于 DDD 四层架构的 Spring Boot 骨架项目，经过代码审计发现以下技术债务：

1. **目录结构**
   - `bizshared/` 和 `common/` 职责重叠
   - `example/` 命名容易被误解为示例代码
   - 四层目录结构不完全对应

2. **代码规范**
   - Domain 层存在 FastJSON 外部依赖
   - 异常处理未使用项目定义的异常体系
   - 日志格式不统一

3. **代码质量**
   - SearchServiceImpl 787行，职责过多
   - DTO 转换使用多种模式（MapStruct、静态方法、手动转换）
   - 存在"My"前缀等不规范命名

### 约束

- 必须保持向后兼容（API 不变）
- 必须通过所有现有测试
- 重构过程不能影响生产环境

## Goals / Non-Goals

**Goals:**

1. 建立清晰的目录结构规范，`bizshared` → `shared`，`common` → `platform`
2. 修复所有宪法级别的规范违规
3. 统一异常处理、DTO 转换、日志格式
4. 拆分大类，提高代码可维护性
5. 规范化命名，移除"My"前缀

**Non-Goals:**

1. 不改变现有 API 接口签名
2. 不修改业务逻辑
3. 不引入新的外部依赖（除替换 actuator）
4. 不重构 exampleorder 业务模块的业务规则

## Decisions

### D1: 目录重命名策略

**决定**: 使用 IDE 重构功能批量重命名包

**方案对比**:

| 方案 | 优点 | 缺点 |
|------|------|------|
| **IDE 批量重构** ✅ | 自动更新所有引用，安全 | 需要 IDE 支持 |
| 手动修改 | 无工具依赖 | 容易遗漏，风险高 |
| 脚本替换 | 可自动化 | 需要处理边界情况 |

**执行顺序**:
1. `bizshared` → `shared`（4个模块）
2. `common` → `platform`（4个模块）
3. `example` → `exampleorder`（4个模块）

### D2: FastJSON 依赖移除方案

**决定**: 将序列化逻辑抽象为接口，由 Infrastructure 层实现

**当前代码**:
```java
// domain/.../event/Type.java
import com.alibaba.fastjson2.JSON;

public enum Type {
    UNKNOW("未知事件", Source.DOMAIN, JSON::parseObject),
    // ...
    public String toJsonString(Object payload) {
        return JSON.toJSONString(payload);
    }
}
```

**重构方案**:
```java
// domain/.../event/Type.java - 移除 FastJSON 依赖
public enum Type {
    UNKNOW("未知事件", Source.DOMAIN),
    // ...
    
    // 移除 JSON 相关方法，改为抽象接口
    public interface PayloadParser {
        <T> T parse(String json, Class<T> type);
    }
}

// infrastructure/.../event/FastJsonPayloadParser.java
public class FastJsonPayloadParser implements Type.PayloadParser {
    @Override
    public <T> T parse(String json, Class<T> type) {
        return JSON.parseObject(json, type);
    }
}
```

### D3: 异常类型统一方案

**决定**: 全局替换 + 验证

**映射规则**:

| 当前异常 | 替换为 | 场景 |
|----------|--------|------|
| `IllegalArgumentException` | `BizException` | Domain 层业务验证 |
| `IllegalStateException` | `BizException` | Domain 层状态验证 |
| `RuntimeException` | `SysException` | Infrastructure 层技术异常 |

**实现方式**:
```java
// 之前
if (order.getStatus() != OrderStatus.CREATED) {
    throw new IllegalStateException("只有已创建的订单可以支付");
}

// 之后
if (order.getStatus() != OrderStatus.CREATED) {
    throw new BizException(OrderErrorCode.INVALID_STATUS, 
        "订单状态无效，当前状态：" + order.getStatus());
}
```

### D4: DTO 转换统一方案

**决定**: 全部使用 MapStruct，移除静态 fromDTO 方法

**转换器分布**:

| 层 | 转换器位置 | 职责 |
|---|-----------|------|
| Adapter | `adapter/.../converter/` | Request → Command, DTO → Response |
| App | `app/.../converter/` | Domain → DTO |
| Infrastructure | `infrastructure/.../converter/` | Domain ↔ DO |

**示例**:
```java
// adapter/.../converter/OrderRequestConverter.java
@Mapper(componentModel = "spring")
public interface OrderRequestConverter {
    CreateOrderCommand toCommand(CreateOrderRequest request);
    OrderResponse toResponse(OrderDTO dto);
}
```

### D5: 日志格式统一方案

**决定**: 使用 LogAspect 自动添加元信息

**标准格式**:
```
[类型] 类#方法 | 业务描述 | 耗时ms | 线程 | 入参 | 出参/错误
```

**实现**:
- LogAspect 自动添加：类、方法、耗时、线程、traceId
- 开发者只需关注：业务描述、入参、出参

```java
// 之前
log.info("订单创建成功: orderNo={}", orderNo);

// 之后
log.info("[ORDER] 创建订单成功 | orderNo={}", orderNo);
// LogAspect 自动补充：类#方法 | 耗时ms | 线程 | traceId
```

### D6: 大类拆分方案

**决定**: SearchServiceImpl 按职责拆分

```
SearchServiceImpl (787行)
    ↓ 拆分为
├── SearchDslBuilder        # DSL 构建逻辑 (~200行)
├── SearchResultConverter   # 结果转换逻辑 (~150行)  
├── VectorSearchService     # 向量搜索 (~100行)
├── AiSearchService         # AI 搜索 (~100行)
└── SearchServiceImpl       # 协调逻辑 (~150行)
```

## Risks / Trade-offs

### R1: 包重命名导致 Git 历史断裂

**风险**: 重命名后难以追踪文件历史

**缓解**: 
- 在一次 commit 中完成所有重命名
- commit message 明确记录重命名映射

### R2: 异常类型替换影响异常处理逻辑

**风险**: 某些代码可能依赖异常类型进行判断

**缓解**:
- 全局搜索 `catch (IllegalArgumentException` 等模式
- 验证 GlobalExceptionHandler 能正确处理新异常类型

### R3: DTO 转换器改动影响测试

**风险**: MapStruct 生成代码可能与手动转换结果不一致

**缓解**:
- 为每个 Converter 编写单元测试
- 对比转换前后的结果

## Migration Plan

### 阶段一：目录结构重组 (1天)

```bash
# 1. 备份当前代码
git checkout -b refactor/package-restructure

# 2. 执行重命名（IDE Refactor）
# - bizshared → shared
# - common → platform
# - example → exampleorder

# 3. 验证编译
mvn clean compile

# 4. 运行测试
mvn test

# 5. 提交
git commit -m "refactor: restructure package naming"
```

### 阶段二：规范违规修复 (1天)

```bash
# 1. 创建 feature 分支
git checkout -b refactor/constitution-fixes

# 2. 移除 FastJSON 依赖
# 3. 替换异常类型
# 4. 替换 actuator 依赖

# 5. 验证
mvn clean compile && mvn test

# 6. 提交
git commit -m "fix: resolve constitution violations"
```

### 阶段三：模式统一 (1天)

```bash
# 1. 创建 MapStruct 转换器
# 2. 移除静态 fromDTO 方法
# 3. 统一日志格式

# 4. 验证
mvn clean compile && mvn test

# 5. 提交
git commit -m "refactor: unify coding patterns"
```

### 阶段四：代码质量提升 (1天)

```bash
# 1. 拆分大类
# 2. 移除"My"前缀
# 3. 修复 TODO

# 4. 验证
mvn clean compile && mvn test
mvn test -Dtest=ApplicationStartupTests -pl test

# 5. 提交
git commit -m "refactor: improve code quality"
```

### 回滚策略

每个阶段独立提交，如发现问题可单独回滚：
```bash
git revert <commit-hash>
```

## Open Questions

1. ~~client 是否改为 gateway~~ → **决定**: 保持 `client` 命名不变
2. 是否需要为 errorCode 创建统一的枚举基类？
3. LogAspect 是否需要支持异步日志？
4. SearchServiceImpl 拆分后是否需要门面模式？

---

**文档版本**: 1.0
**更新日期**: 2026-02-12
