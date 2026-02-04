# PROJECT KNOWLEDGE BASE

**生成时间**: 2026-02-03
**Commit**: e24817a
**Branch**: main

## 文档导航

### 📚 项目相关文档

| 文档                                             | 用途                       | 读者           |
|------------------------------------------------|--------------------------|--------------|
| **[项目 README](README.md)**                     | 项目概览、架构说明和快速开始           | 开发者、架构师      |
| **[AI 开发指南](CLAUDE.md)**                       | AI 开发元指南（TDD 工作流和编码规则）   | 开发者、AI       |
| **[Maven Archetype 使用指南](ARCHETYPE_USAGE.md)** | 快速生成基于 DDD 架构的 Java 项目骨架 | 开发者          |
| **[规格文档索引](_docs/specification/README.md)**    | 项目开发规范和指南文档（编码、测试、验证）    | 开发者、AI       |
| **[业务文档索引](_docs/business/README.md)**         | 业务开发文档（需求、设计、技术选型等）      | 产品经理、架构师、开发者 |

### 📚 各层 AGENTS.md 指南

| 文档                                                 | 用途                | 读者    |
|----------------------------------------------------|-------------------|-------|
| **[Domain 层指南](domain/AGENTS.md)**                 | 领域层核心业务逻辑和约定      | 后端开发者 |
| **[Infrastructure 层指南](infrastructure/AGENTS.md)** | 基础设施层技术实现和约定      | 后端开发者 |
| **[Application 层指南](app/AGENTS.md)**               | 应用层 CQRS 和用例编排    | 后端开发者 |
| **[Adapter 层指南](adapter/AGENTS.md)**               | 接口层 REST 控制器和事件监听 | 后端开发者 |
| **[Start 模块指南](start/AGENTS.md)**                  | 启动模块 Bean 装配和配置   | 后端开发者 |
| **[Test 模块指南](test/AGENTS.md)**                    | 测试模块测试规范和最佳实践     | 测试开发者 |

---

## 概述

符合 DDD 规范的 Maven 多模块 Java 项目（JDK 25, Spring Boot 4.0.2），实现四层架构（Domain-App-Infra-Adapter）和 CQRS、事件驱动模式。

## 结构

```
web-quick-start-domain/
├── adapter/          # 接口适配层（Controller、Listener、Scheduler）
├── app/             # 应用编排层（CQRS、Command、Query）
├── domain/           # 核心业务逻辑（Aggregate、Entity、VO、Event）
├── infrastructure/    # 技术实现（Repository、Cache、Search、OSS）
├── start/           # 启动模块（Bean 组装通过 *Configure 类）
├── test/            # 独立测试模块，启动验证
├── _docs/           # 项目文档和规范
└── pom.xml          # 根 Maven POM
```

## 关键位置

| 任务            | 位置                                                                     | 备注                               |
|---------------|------------------------------------------------------------------------|----------------------------------|
| 入口点           | start/src/main/java/org/smm/archetype/ApplicationBootstrap.java        | Spring Boot 主类，CommandLineRunner |
| 领域模型          | domain/src/main/java/org/smm/archetype/domain/                         | 聚合根、实体、值对象                       |
| 应用服务          | app/src/main/java/org/smm/archetype/app/                               | CQRS 编排                          |
| Repository 实现 | infrastructure/src/main/java/org/smm/archetype/infrastructure/         | MyBatis-Flex Mapper              |
| Controller    | adapter/src/main/java/org/smm/archetype/adapter/                       | REST 端点                          |
| Bean 配置       | start/src/main/java/org/smm/archetype/config/                          | 所有 *Configure 类                  |
| 测试验证          | test/src/test/java/org/smm/archetype/test/ApplicationStartupTests.java | 启动集成测试                           |

## 代码映射

(暂无 - LSP 符号未查询)

## 约定（非标准）

**非标准命名（项目特定风格）**：

- 配置类：`*Configure`（如 `OrderConfigure`，而非 `OrderConfig`）
- 共享包：`bizshared`（而非 `shared`/`common`）
- 示例代码：`_example/` 前缀在生产模块内

**标准 DDD 分层**：

- Adapter → Application → Domain ← Infrastructure（依赖规则）
- Domain 层：无外部依赖，纯业务逻辑
- Repository 接口在 Domain 层，实现在 Infrastructure 层

**配置规则**：

- 所有 `@Configuration` 类必须在 `start/src/main/java/org/smm/archetype/config/`
- 命名：`{Aggregate}Configure`（如 `OrderConfigure`）
- Bean 组装仅通过 `@Bean` 方法（无 `@Component` 扫描）

**Lombok 规则**：

- `@Data`：❌ 禁止（不可控的代码生成）
- 使用：`@Getter`、`@Setter`、`@Builder`、`@RequiredArgsConstructor`

## 反模式（本项目）

**禁止模式**：

1. `@Data` 注解 - 不可控的 equals/hashCode 生成
2. `@Lazy`、`ObjectProvider` 用于循环依赖 - 必须重构
3. 配置类在 `start/` 模块外 - 仅 `start/config/` 允许
4. 测试在生产模块中 - 必须使用独立的 `test/` 模块
5. Domain 层的外部依赖 - Domain 必须保持纯净

## 独特风格

**事件驱动架构**：

- 自动检测：Spring Events（默认）vs Kafka（基于 Bean 存在条件判断）
- 重试策略：指数退避、外部调度器（支持 XXL-JOB/PowerJob）

**测试优先验证**：

- 强制 4 步验证：编译 → 测试 → 启动 → 覆盖率
- 独立测试模块，包含 `ApplicationStartupTests` 用于 Spring 上下文验证

**Bean 组装模式**：

- 构造函数注入优先（`@RequiredArgsConstructor`）
- `@Bean` 方法参数用于配置内依赖
- 跨配置循环依赖：Optional/`@ConditionalOnBean`

## 命令

```bash
# 编译
mvn clean compile

# 测试（含 JaCoCo 覆盖率，需要 JDK 25）
mvn test

# 启动验证（最关键）
mvn test -Dtest=ApplicationStartupTests -pl test

# 运行应用
mvn spring-boot:run -pl start

# 生成 archetype（用于新项目）
mvn archetype:generate -DarchetypeGroupId=org.smm.archetype -DarchetypeArtifactId=web-quick-start-domain -DarchetypeVersion=1.0.0
```

## 注意事项

**偏差警告**（`_docs/specification/业务代码编写规范.md` 文档中已记录）：

- Spring Boot 版本不匹配：pom.xml 显示 4.0.2，README 提及 4.0.2
- MyBatis-Flex：手动依赖管理而非 spring-boot-starter
- 测试结构：独立的 `test/` 模块（非与生产代码共存）

**覆盖率要求**：

- 行覆盖率：≥95%
- 分支覆盖率：100%
- 测试通过率：100%

**关键点**：

- JDK 25 必需（启用虚拟线程）
- 中间件可选：Kafka、Redis、Elasticsearch - 全部通过 `@ConditionalOnBean` 自动检测
- 示例代码（`_example/`）是生产模块的一部分，而非独立模块
- 启动验证测试是 Spring 上下文健康的守门员
