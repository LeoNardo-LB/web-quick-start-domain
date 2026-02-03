# PROJECT KNOWLEDGE BASE

**生成时间**: 2026-02-04 | **Commit**: f5c4a8e | **Branch**: main

## 概述

DDD 规范 Maven 多模块项目（JDK 25, Spring Boot 4.0.2）。
四层架构：Domain-App-Infrastructure-Adapter。CQRS + 事件驱动。

## 模块结构

- `domain/` - 核心业务逻辑（聚合根、实体、值对象、事件）
- `app/` - 应用编排层（CQRS、Command、Query）
- `infrastructure/` - 技术实现（Repository、Cache、Search、OSS）
- `adapter/` - 接口适配层（Controller、Listener、Scheduler）
- `start/` - 启动模块（Bean 组装通过 *Configure 类）
- `test/` - 独立测试模块，启动验证

## 约定

**非标准命名**：`*Configure`（非 `*Config`）、`bizshared`（非 `shared`）、`example/` 前缀

**DDD 分层**：Adapter → App → Domain ← Infrastructure

**Bean 装配**：所有 `@Configuration` 必须在 `start/config/`，仅使用 `@Bean` 方法

**Lombok**：禁止 `@Data`，使用 `@Getter/@Setter/@Builder/@RequiredArgsConstructor`

## 反模式（本项目）

禁止：`@Data`、`@Lazy`/`ObjectProvider`、配置类在 `start/` 外、测试在生产模块、Domain 层外部依赖

## 独特风格

**事件驱动**：Spring Events（默认）/Kafka（基于 Bean 检测）、重试策略（指数退避/外部调度器）
**测试优先**：强制 4 步验证（编译→测试→启动→覆盖率）、独立测试模块
**Bean 组装**：构造函数注入、`@Bean` 参数注入、Optional/`@ConditionalOnBean`

## 命令

```bash
mvn clean compile           # 编译
mvn test                    # 测试（含覆盖率，需 JDK 25）
mvn test -Dtest=ApplicationStartupTests -pl test  # 启动验证
mvn spring-boot:run -pl start  # 运行应用
```

## 注意事项

**覆盖率**：行≥95%、分支100%、通过率100%
**关键点**：JDK 25（虚拟线程）、中间件可选（Kafka/Redis/ES）、示例代码在生产模块内

## 架构偏差（探索发现）

**⚠️ P0 - Domain 层外部依赖违反 DDD 原则**

- `domain/bizshared/util/MyBeanUtil.java` 导入了 Spring 框架类：
  ```java
  import org.springframework.beans.BeanUtils;
  import org.springframework.beans.BeanWrapper;
  import org.springframework.beans.BeanWrapperImpl;
  ```
- **影响**：破坏 DDD 核心原则，领域层耦合 Spring 框架，难以独立测试
- **建议**：重构为纯 Java 实现，使用手动属性拷贝而非 Spring BeanUtils

**⚠️ P0 - @Data 注解使用违反规范**

- 文档明确禁止使用 `@Data`，但实际在以下位置使用：
    - `start/config/properties/` 目录下所有配置属性类（8 个文件）
    - `infrastructure/bizshared/dal/generated/entity/` 下 DO 类（3 个文件）
- **影响**：违反项目核心约束，可能导致不可预期的 equals/hashCode 行为

**⚠️ P1 - @Component 使用违反 Bean 装配规范**

- `adapter/schedule/ExponentialBackoffRetryStrategy.java` 和 `ExternalSchedulerRetryStrategy.java` 使用了 `@Component`
- **影响**：偏离显式 Bean 装配原则，可能导致循环依赖问题
- **建议**：移至 `start/config/` 并通过 `@Bean` 方法装配

**⚠️ P1 - 配置类位置偏差**

- `start/exampleorder/OrderConfigure.java` 在 `start/` 下但不在 `config/` 包下
- **影响**：不符合"配置类集中在 config/"的约定
- **建议**：移至 `start/src/main/java/.../config/example/OrderConfigure.java`

**⚠️ P2 - 包命名非标准**

- 使用 `bizshared` 而非 `shared`，使用 `example` 而非 `_example`
- **影响**：已文档标注为"项目特定风格"，但与标准实践不同
- **说明**：这是有意为之的设计选择，无需修改

**ℹ️ LSP 诊断错误**

- 项目存在多个 LSP 导入解析错误，可能与本地 Maven 仓库配置相关
- 这些错误不影响代码功能，但会影响 IDE 的智能提示

## 项目规模

| 指标        | 数值                                                   |
|-----------|------------------------------------------------------|
| 总文件数      | 263                                                  |
| Java 代码行数 | 19,358                                               |
| 最大目录深度    | 14 层                                                 |
| 核心模块      | 6（adapter, app, domain, infrastructure, start, test） |
