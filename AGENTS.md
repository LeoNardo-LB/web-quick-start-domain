# 项目知识库

**生成时间**: 2026-02-14
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
| Test           | [test/AGENTS.md](test/AGENTS.md)                     | 测试规范、TDD 流程、覆盖率标准      |

## 技术栈

| 分类   | 技术                | 版本          |
|------|-------------------|-------------|
| 语言   | Java              | 25（虚拟线程）    |
| 核心框架 | Spring Boot       | 4.0.2       |
| 持久层  | MyBatis-Flex      | 1.11.5      |
| 对象映射 | MapStruct         | 1.5.5.Final |
| 测试   | JUnit 5 + Mockito | -           |
| 覆盖率  | JaCoCo            | 0.8.14      |

## 验证流程

> **编码前必须加载 TDD skill**：`/tdd-workflow`

### 五阶段验证（NON-NEGOTIABLE）

| 阶段       | 命令                                                             | 说明           |
|----------|----------------------------------------------------------------|--------------|
| 1. 编码验证  | `mvn clean compile`                                            | 确保编译通过       |
| 2. 单元测试  | `python scripts/python/run-unit-tests.py --diff HEAD~1`        | 仅变更关联的单元测试   |
| 3. 集成测试  | `python scripts/python/run-integration-tests.py --diff HEAD~1` | 仅变更关联的集成测试   |
| 4. 覆盖率验证 | `mvn verify -pl test`                                          | 生成 JaCoCo 报告 |
| 5. 抽检测试  | `python scripts/python/run-sample-tests.py`                    | 所有测试中抽取 10%  |

### Maven Lifecycle 说明

执行顺序：`clean` → `compile` → `test` → `package` → `install` → `deploy`

执行某个 phase 时，之前的 phase 会自动执行。

## 质量标准

| 指标      | 标准                  |
|---------|---------------------|
| 测试通过率   | **100%**（禁止提交失败的测试） |
| 单元测试覆盖率 | 行≥95%，分支≥95%        |
| 集成测试覆盖率 | 行≥90%，分支≥80%        |
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
| `mvn verify -pl test`                              | 生成覆盖率报告              |
| `mvn spring-boot:run -pl start`                    | 启动应用                 |

## 相关文档

- [README](README.md) - 项目概览、快速开始
- TDD 验证流程 - 使用 `/tdd-workflow` 命令加载

---

**版本**: 2.0 | **整合自**: CONSTITUTION.md + 原 AGENTS.md
