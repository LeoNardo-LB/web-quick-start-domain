# 项目知识库

**生成时间**: 2026-02-18
**Commit**: 6a4d596
**Branch**: feat/mybatis-plus-migration
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

### 四阶段验证（NON-NEGOTIABLE）

| 阶段        | 触发时机        | 命令                                  | 通过标准    |
|-----------|-------------|-------------------------------------|---------|
| 1. LSP 检查 | 每个文件/小功能完成  | `lsp_diagnostics`                   | 零错误     |
| 2. 单元测试   | TODOLIST 完成 | `mvn test -Dtest=XxxUTest -pl test` | 100% 通过 |
| 3. 集成测试   | 所有开发完成      | `mvn test -Dtest=XxxITest -pl test` | 100% 通过 |
| 4. 抽检     | 单测+集测通过后    | 抽检 10%                              | 100% 通过 |

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

## Speccoding 质量记分板

| 维度       | 目标 | 当前     | 保障机制                  |
|----------|----|--------|-----------------------|
| **规范性**  | 10 | **10** | OpenSpec + Spec 覆盖率脚本 |
| **稳定性**  | 10 | **10** | TDD 四阶段 + ArchUnit 测试 |
| **一致性**  | 10 | **10** | 编码规范 + Speccoding 规则  |
| **职责分明** | 10 | **10** | DDD 四层 + 模块边界文档       |

---

**版本**: 2.8 | **更新**: 2026-02-18
