# DDD Architecture Refactoring Proposal

## Why

当前项目存在多个架构和代码质量问题，影响代码的可读性、可维护性和架构纯净性：

1. **目录结构混乱**：`bizshared` vs `common` 职责重叠，`example` 命名不清晰
2. **规范违规**：Domain层存在外部依赖、异常类型未遵循规范、日志格式不统一
3. **代码质量**：命名不清晰、模式不一致、存在大类和重复代码

这些问题会导致新人难以理解代码结构，增加维护成本，需要系统性重构。

## What Changes

### A. 目录结构重组 **BREAKING**

| 当前 | 优化后 | 说明 |
|------|--------|------|
| `bizshared/` | `shared/` | 跨模块共享基础设施 |
| `common/` | `platform/` | 平台级通用能力 |
| `example/` | `exampleorder/` | 订单业务模块（真实业务名） |

### B. 规范违规修复

- **P0**: 移除Domain层FastJSON依赖 (`Type.java`)
- **P1**: errorCode String → 枚举类型 (4个文件)
- **P2**: 替换spring-boot-starter-actuator为具体依赖
- **P3**: 测试代码字段注入改为构造器注入

### C. 命名规范化

- 移除"My"前缀：`MyContext` → `ScopedThreadContext`
- 通用名称优化：`Log` → `MethodExecutionLog`
- 方法语义增强：`handle()` → `handleOrderCreatedEvent()`
- 布尔风格修正：`createUser` → `createdBy`

### D. 模式统一化

- **异常处理**：统一使用 BizException/SysException/ClientException
- **DTO转换**：统一使用 MapStruct
- **日志格式**：统一为 `[类型] 类#方法 | 业务描述 | 耗时ms | 线程 | 入参 | 出参/错误`
- **构造函数**：统一使用 @RequiredArgsConstructor

### E. 代码质量提升

- **拆分大类**：SearchServiceImpl(787行) → 多个职责单一的类
- **消除重复**：提取公共方法、使用模板模式
- **修复TODO**：实现traceId获取、库存释放逻辑
- **移除调试代码**：Domain层System.out.println

## Capabilities

### New Capabilities

- `package-structure`: 标准化的DDD四层目录结构规范
- `exception-handling`: 统一的异常处理体系（BizException/SysException/ClientException）
- `dto-conversion`: 基于MapStruct的统一DTO转换规范
- `logging-standard`: 统一的日志格式和记录规范

### Modified Capabilities

- `ddd-layers`: 四层架构的目录命名和职责划分变更
- `naming-convention`: 命名规范的强化和补充

## Impact

### 受影响的代码

| 模块 | 影响范围 | 说明 |
|------|----------|------|
| **domain** | ~82个文件 | 目录重组、异常类型、日志格式 |
| **infrastructure** | ~43个文件 | 目录重组、MapStruct转换器 |
| **app** | ~15个文件 | 目录重组、DTO转换重构 |
| **adapter** | ~20个文件 | 目录重组、异常处理、命名修正 |
| **start** | 配置类 | Bean引用路径更新 |
| **test** | ~8个文件 | 包引用更新、字段注入修正 |

### 破坏性变更

1. **包路径变更**：所有import语句需要更新
2. **类名变更**：MyContext、MyLog等类重命名
3. **异常类型变更**：IllegalArgumentException → BizException

### 风险评估

- **低风险**：目录重命名（IDE自动重构）
- **中风险**：异常类型替换（需要逐个验证）
- **高风险**：MapStruct统一转换（需要全面测试）

### 依赖影响

- 需要删除：`spring-boot-starter-actuator`
- 需要新增：无（MapStruct已存在）
- 需要修改：`adapter/pom.xml`

---

**预计工作量**：3-5人天
**建议执行方式**：分阶段进行，每阶段完成后验证测试通过
