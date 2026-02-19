# 项目知识库

**生成时间**: 2026-02-19
**Commit**: cbcf343
**Branch**: main
**项目**: DDD Web Quick Start Domain
**架构**: 四层 DDD (Domain-Driven Design)

## 项目概览

基于 Spring Boot 4.0.2 + Java 25 的 DDD 架构骨架项目，严格遵循领域驱动设计原则。包含 6 个 Maven
模块：domain、app、infrastructure、adapter、start、test。

**核心特点**：
- 严格的四层依赖方向：Adapter → Application → Domain ← Infrastructure
- 纯净的 Domain 层：零外部依赖，仅包含业务逻辑
- CQRS 模式：命令查询分离
- 事件驱动架构：支持 Kafka 和 Spring 事件

## 目录结构

```
web-quick-start-domain/
├── domain/              # 领域层（聚合根、实体、值对象、仓储接口）
├── app/                 # 应用层（ApplicationService、CQRS、DTO转换）
├── infrastructure/      # 基础设施层（Repository实现、EventPublisher、缓存、文件服务）
├── adapter/             # 接口层（Controller、EventListener、异常处理）
├── start/               # 启动模块（所有配置类、Bean装配）
├── test/                # 测试模块（独立测试模块，单元测试、集成测试）
└── openspec/            # OpenSpec 规范目录
```

## 四层架构原则

```
┌─────────────────────────────────────────────────────────────┐
│                        Adapter 层                            │
│  (Controller、EventListener、Schedule、Request/Response DTO)  │
└────────────────────┬────────────────────────────────────────┘
                     │ 依赖
┌────────────────────▼────────────────────────────────────────┐
│                     Application 层                             │
│         (ApplicationService、CQRS、DTO转换、事务管理)          │
└────────────────────┬────────────────────────────────────────┘
                     │ 依赖
┌────────────────────▼────────────────────────────────────────┐
│                      Domain 层                               │
│     (聚合根、实体、值对象、领域事件、仓储接口、领域服务)         │
│                      ↕                                      │
│              (纯净业务逻辑，无外部依赖)                        │
└────────────────────┬────────────────────────────────────────┘
                     │ 接口
┌────────────────────▼────────────────────────────────────────┐
│                  Infrastructure 层                            │
│   (Repository实现、EventPublisher、CacheService、外部服务)      │
└─────────────────────────────────────────────────────────────┘
```

**依赖规则**：Adapter → Application → Domain ← Infrastructure

## 模块指南

| 模块             | 文档                                                   | 核心职责                   |
|----------------|------------------------------------------------------|------------------------|
| Domain         | [domain/AGENTS.md](domain/AGENTS.md)                 | 聚合根、值对象、仓储接口、领域事件      |
| Application    | [app/AGENTS.md](app/AGENTS.md)                       | CQRS 编排、事务边界、DTO 转换    |
| Infrastructure | [infrastructure/AGENTS.md](infrastructure/AGENTS.md) | Repository 实现、事件发布、中间件 |
| Adapter        | [adapter/AGENTS.md](adapter/AGENTS.md)               | REST 接口、异常处理、参数验证      |
| Start          | [start/AGENTS.md](start/AGENTS.md)                   | 配置集中化、Bean 装配、线程池      |
| Test           | [test/AGENTS.md](test/AGENTS.md)                     | 测试规范、TDD 流程            |

## 技术栈

| 分类     | 技术                         | 版本          | 说明          |
|--------|----------------------------|-------------|-------------|
| 语言     | Java                       | 25（虚拟线程）    |             |
| 核心框架   | Spring Boot                | 4.0.2       |             |
| 持久层    | MyBatis Plus               | 3.5.16      | ORM 框架      |
| 对象映射   | MapStruct                  | 1.5.5.Final | DTO/DO 转换   |
| 工具库    | Hutool                     | 5.8.41      | 通用工具类       |
| 工具库    | Lombok                     | 1.18.42     | 代码简化        |
| API 文档 | SpringDoc OpenAPI          | 3.0.1       | OpenAPI 3.0 |
| JSON   | Jackson                    | 2.19.4      | JSON 序列化    |
| 日志     | Logback + Logstash Encoder | 7.4         | 日志框架        |
| 测试     | JUnit 5 + Mockito          | -           | 单元测试        |

## 验证流程

> **TDD 流程规范**: `openspec/config.yaml` → **测试规范**: `test/AGENTS.md`

### 阶段式测试流程（NON-NEGOTIABLE）

| 阶段        | 触发时机    | 命令                                                 | 通过标准    |
|-----------|---------|----------------------------------------------------|---------|
| 1. LSP 检查 | 每个文件完成后 | `lsp_diagnostics`                                  | 零错误     |
| 2. 编译验证   | 模块实现完成后 | `mvn clean compile`                                | 编译成功    |
| 3. 单元测试   | 模块测试阶段  | `mvn test -Dtest=XxxUTest -pl test`                | 100% 通过 |
| 4. 集成测试   | 所有模块完成后 | `mvn test -Dtest=XxxITest -pl test`                | 100% 通过 |
| 5. 启动验证   | 所有测试通过后 | `mvn test -Dtest=ApplicationStartupTests -pl test` | 通过      |

**效率优化**：实现阶段保持连贯，测试阶段集中验证，避免高频打断。

### Maven Lifecycle 说明

执行顺序：`clean` → `compile` → `test` → `package` → `install` → `deploy`

执行某个 phase 时，之前的 phase 会自动执行。

## 质量标准

| 指标      | 标准                  |
|---------|---------------------|
| 测试通过率   | **100%**（禁止提交失败的测试） |
| 编译警告    | 零警告（Werror 策略）      |
| DDD 符合度 | ≥8.6/10             |

## Lombok 使用规范

| 规则                            | 说明                                    |
|-------------------------------|---------------------------------------|
| 禁止 `@Data`                    | 生成不可控的 `equals/hashCode`，导致性能问题       |
| 推荐 `@Builder`                 | 对象构建，确保不可变性                           |
| 推荐 `@RequiredArgsConstructor` | 配合 `final` 字段进行依赖注入                   |
| 值对象                           | 必须使用 `@Builder` 模式                    |
| Response                      | 必须使用 `@Builder(setterPrefix = "set")` |

## Maven 依赖管理

| 规则         | 说明                                            |
|------------|-----------------------------------------------|
| 版本管理       | 所有依赖版本必须在根 POM `<dependencyManagement>` 中统一管理 |
| 子模块        | 禁止指定依赖版本，直接使用 `groupId:artifactId`            |
| 注解处理器      | 必须在根 POM `<annotationProcessorPaths>` 中配置     |
| 禁止 starter | 禁止使用 `spring-boot-starter` 引入传递依赖，手动指定具体依赖    |

## 编码风格规范

### 命名规范

| 类型 | 规则          | 示例                                      |
|----|-------------|-----------------------------------------|
| 类名 | PascalCase  | `OrderAggr`, `OrderRepository`          |
| 接口 | 无 I 前缀      | `OrderRepository`（非 `IOrderRepository`） |
| 方法 | 动词 + 名词     | `createOrder()`, `findById()`           |
| 变量 | camelCase   | `orderList`, `customerId`               |
| 常量 | UPPER_SNAKE | `MAX_RETRY`, `DEFAULT_SIZE`             |
| 包名 | 全小写         | `org.smm.archetype.domain.order`        |

### 方法动词表

| 操作   | 前缀       | 示例                           |
|------|----------|------------------------------|
| 单条查询 | find/get | `findById()`, `getOrderNo()` |
| 列表查询 | list     | `listByStatus()`             |
| 存在判断 | exists   | `existsByOrderNo()`          |
| 数量统计 | count    | `countByCustomer()`          |
| 创建   | create   | `createOrder()`              |
| 更新   | update   | `updateStatus()`             |
| 删除   | delete   | `deleteById()`               |
| 保存   | save     | `save()`                     |

### Import 顺序

```java
// 1. java.* / javax.*

import java.time.Instant;
import java.util.List;

// 2. 第三方（按字母排序）
import lombok.Builder;
import org.springframework.stereotype.Service;

// 3. 本项目
import org.smm.archetype.domain.order.OrderAggr;

// 4. 静态（最后）
import static org.assertj.core.api.Assertions.assertThat;
```

### 注释规范

| 元素   | 要求                   | 语言 |
|------|----------------------|----|
| 公共类  | Javadoc 描述职责         | 中文 |
| 公共方法 | Javadoc 描述功能 + 参数/返回 | 中文 |
| 复杂逻辑 | 行内注释说明原因             | 中文 |
| 简单方法 | 可省略                  | -  |

### 日志规范

| 级别    | 场景   | 格式                                 |
|-------|------|------------------------------------|
| DEBUG | 详细流程 | `log.debug("查询: id={}", id)`       |
| INFO  | 关键操作 | `log.info("创建成功: orderNo={}", no)` |
| WARN  | 边界情况 | `log.warn("状态异常: {}", status)`     |
| ERROR | 异常失败 | `log.error("操作失败: {}", id, e)`     |

**禁止**：日志中包含敏感信息（密码、token、身份证）

### 异常规范

| 类型                | HTTP | 场景       |
|-------------------|------|----------|
| `BizException`    | 400  | 可预期的业务错误 |
| `ClientException` | 4xx  | 外部服务调用失败 |
| `SysException`    | 500  | 系统内部错误   |

**消息格式**：`{操作}失败: {原因}` → `"支付失败: 余额不足"`

## 全局反模式（禁止）

| ❌ 禁止                         | ✅ 正确做法                                     | 规范位置                               |
|------------------------------|--------------------------------------------|------------------------------------|
| 使用 `@Lazy` 解决循环依赖            | 重构解决                                       | [start/AGENTS.md](start/AGENTS.md) |
| Domain 层使用 `@Transactional`  | 事务边界在 Application 层                        | [app/AGENTS.md](app/AGENTS.md)     |
| 使用 `@Data` 注解                | 使用 `@Builder` + `@RequiredArgsConstructor` | 本文档                                |
| 直接使用 `mvn test` 运行测试         | 使用 TDD 脚本                                  | [test/AGENTS.md](test/AGENTS.md)   |
| adapter/infrastructure 创建配置类 | 配置类在 start 模块                              | [start/AGENTS.md](start/AGENTS.md) |
| 单元测试启动 Spring 上下文            | 使用纯 Mock                                   | [test/AGENTS.md](test/AGENTS.md)   |

## 常用命令

| 命令                                                 | 说明                   |
|----------------------------------------------------|----------------------|
| `mvn clean compile`                                | 编译验证                 |
| `mvn test -Dtest=ApplicationStartupTests -pl test` | 启动验证（Spring 上下文健康检查） |
| `mvn spring-boot:run -pl start`                    | 启动应用                 |

## 新模块开发指南

> **开发顺序**：Domain → Infrastructure → Application → Adapter → Start → Test

### 步骤概览

| 步骤 | 模块             | 产出                           | 参考文档                                                 |
|----|----------------|------------------------------|------------------------------------------------------|
| 1  | Domain         | 聚合根、实体、仓储接口、领域服务             | [domain/AGENTS.md](domain/AGENTS.md)                 |
| 2  | Infrastructure | Repository 实现、Converter      | [infrastructure/AGENTS.md](infrastructure/AGENTS.md) |
| 3  | Application    | Command、Query、DTO、AppService | [app/AGENTS.md](app/AGENTS.md)                       |
| 4  | Adapter        | Controller、Request、Response  | [adapter/AGENTS.md](adapter/AGENTS.md)               |
| 5  | Start          | Configure 配置类                | [start/AGENTS.md](start/AGENTS.md)                   |
| 6  | Test           | 单元测试、集成测试                    | [test/AGENTS.md](test/AGENTS.md)                     |

### 快速清单

**Domain 层**：

```
domain/src/main/java/org/smm/archetype/domain/{模块}/
├── model/
│   ├── {Entity}Aggr.java        # 聚合根
│   ├── {Entity}.java            # 实体
│   └── valueobject/             # 值对象
├── repository/
│   └── {Entity}Repository.java  # 仓储接口
└── service/
    └── {Entity}DomainService.java # 领域服务
```

**Infrastructure 层**：

```
infrastructure/src/main/java/org/smm/archetype/infrastructure/{模块}/
├── persistence/
│   └── {Entity}RepositoryImpl.java
└── {Entity}Converter.java
```

**Application 层**：

```
app/src/main/java/org/smm/archetype/app/{模块}/
├── command/{UseCase}Command.java
├── query/{UseCase}Query.java
├── dto/{Entity}DTO.java
└── {Entity}AppService.java
```

**Adapter 层**：

```
adapter/src/main/java/org/smm/archetype/adapter/web/
├── api/{Entity}Controller.java
├── dto/request/{UseCase}Request.java
└── dto/response/{Entity}Response.java
```

**Start 层**：

```
start/src/main/java/org/smm/archetype/config/{Entity}Configure.java
```

## 相关文档

- [README](README.md) - 项目概览、快速开始
- [TDD 流程规范](openspec/config.yaml) - 四阶段验证流程
- [测试规范](test/AGENTS.md) - 单测/集测编写规范

## Speccoding 工作流（NON-NEGOTIABLE）

> **规范驱动开发**：先规范，后实现。**每阶段必须澄清需求，用户确认后方可继续。**

### 变更流程

```
/opsx-explore → /opsx-new → /opsx-continue → /opsx-continue → /opsx-apply → /opsx-archive
    探索         创建变更       设计+澄清        任务+澄清        TDD实现       归档
```

| 阶段 | 产出                 | 强制要求                                                    |
|----|--------------------|---------------------------------------------------------|
| 探索 | 探索报告               | **必须**先了解 `openspec/specs/`、`AGENTS.md`、现有代码            |
| 创建 | proposal.md        | 基于探索提出方案，**必须澄清问题**                                     |
| 设计 | design.md + specs/ | **必须与用户确认**业务逻辑、边界条件、异常处理                               |
| 任务 | tasks.md           | **必须与用户确认**优先级、粒度、验收标准                                  |
| 实现 | 代码 + 测试            | 🔴 Red → 🟢 Green → 🔵 Refactor，每个文件后 `lsp_diagnostics` |
| 归档 | archive/           | 验证通过后方可归档                                               |

### 自动触发

| 触发词                 | 示例       | 操作                |
|---------------------|----------|-------------------|
| "添加"/"新增"/"实现" + 功能 | "添加用户登录" | → `/opsx-explore` |
| "修改"/"重构"/"优化"      | "重构订单模块" | → `/opsx-explore` |
| 查询/小修复/格式化          | "这个类做什么" | 直接执行              |

### 主动澄清（核心规则）

**必须暂停反问的情况**：需求模糊 | 业务规则未明确 | 技术方案有多选 | 优先级未定

**澄清示例**：

```
AI: 澄清几个问题：
1. 退款是否需要审核流程？
2. 部分退款是否支持？
3. 退款有效期限制？
```

**继续条件**：用户说"继续"/"可以"/"符合预期" → 下一阶段 | "等等"/"有问题" → 暂停

### 禁止

| ❌ 禁止                 | ✅ 正确                       |
|----------------------|----------------------------|
| 假设用户意图               | 询问："你的意思是 X 还是 Y？"         |
| 自动跳过澄清               | 每阶段至少问一个澄清问题               |
| 先实现后测试               | TDD：Red → Green → Refactor |
| 跳过 `lsp_diagnostics` | 每个文件完成后必须验证                |

---

**版本**: 2.9 | **更新**: 2026-02-19
