# 补充计划 - 添加 optional 标记到 spring-kafka 依赖

## 背景

在审查现有计划时，发现 **Task 10 的描述存在误解**：

### 原始描述
```
- [x] 10. 移除 infrastructure/pom.xml 中的 optional 标记
```

### 实际情况
经过代码审查，`infrastructure/pom.xml` 中的 `spring-kafka` 依赖**没有** `<optional>true</optional>` 标记。

**当前配置** (infrastructure/pom.xml 第58-62行):
```xml
<!-- Kafka（分布式事件驱动） -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <!-- 缺少 <optional>true</optional> 标记 -->
</dependency>
```

### 问题分析

**为什么需要添加 optional 标记**：
1. **设计原则违反**：项目架构要求"自动检测 + 可选依赖"的设计理念
   - 引入中间件依赖 → 自动检测并使用
   - 不引入中间件依赖 → 使用本地组件
   - **当前状态**：所有依赖 infrastructure 的项目都会强制引入 spring-kafka（无论是否需要）

2. **强制依赖的问题**：
   - 即使使用方只想用本地事件发布器（Spring Events）
   - 也会被强制引入 Kafka 相关依赖（spring-kafka、kafka-clients 等）
   - 增加不必要的依赖和潜在的类冲突
   - 违背"可选依赖"的语义

3. **与其他中间件不一致**：
   - 其他中间件依赖（如 Redis、Elasticsearch）没有 optional 标记
   - 但这些是 Spring Boot 标准 Starter，由 Spring Boot 自动管理
   - Kafka 事件发布是可选功能，应该有 optional 标记

---

## 修正后的任务

### Task 10: 添加 optional 标记到 infrastructure/pom.xml 中的 spring-kafka 依赖

**文件**: `infrastructure/pom.xml`

**位置**: 第58-62行

**修改前**:
```xml
<!-- Kafka（分布式事件驱动） -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**修改后**:
```xml
<!-- Kafka（分布式事件驱动） -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <optional>true</optional>
</dependency>
```

**修改说明**:
- 在 `<artifactId>spring-kafka</artifactId>` 后添加 `<optional>true</optional>` 标记
- 保留注释和其他依赖配置不变

---

## 执行步骤

### 步骤1: 验证当前配置
```bash
cd "D:\Develop\code\mine\archetype\web-quick-start-domain"
grep -A 5 "spring-kafka" infrastructure/pom.xml
```

### 步骤2: 修改 infrastructure/pom.xml

**选项A: 手动修改**
1. 打开 `infrastructure/pom.xml`
2. 找到第58-62行（spring-kafka 依赖）
3. 在 `<artifactId>spring-kafka</artifactId>` 后添加 `<optional>true</optional>`

**选项B: 使用 /start-work**
```bash
/start-work
```
然后 Sisyphus（执行器）会自动执行计划中的所有任务。

---

## 预期结果

### Maven 依赖树变化

**修改前**（app 模块）:
```xml
[INFO] com.smm.archetype:app:jar:1.0.0
[INFO] +- org.smm.archetype:infrastructure:jar:1.0.0:compile
[INFO] |  \- org.springframework.kafka:spring-kafka:jar:3.2.2:compile
[INFO] |     \- org.springframework.kafka:kafka-clients:jar:3.7.1:compile
[INFO] |     \- org.springframework.kafka:spring-kafka:jar:3.2.2:compile
```

**修改后**（app 模块，可选依赖）:
```xml
[INFO] com.smm.archetype:app:jar:1.0.0
[INFO] +- org.smm.archetype:infrastructure:jar:1.0.0:compile
[INFO] |  \- (org.springframework.kafka:spring-kafka:jar:3.2.2:compile) - optional
[INFO] |     \- org.springframework.kafka:kafka-clients:jar:3.7.1:compile
[INFO] |     \- org.springframework.kafka:spring-kafka:jar:3.2.2:compile
```

注意：`(org.springframework.kafka:spring-kafka:jar:3.2.2:compile) - optional` 表示该依赖是可选的。

### 应用启动行为变化

**修改前**:
- 如果不引入 spring-kafka 依赖 → 无法启动（强制依赖）
- 如果引入 spring-kafka 依赖 → 强制使用 Kafka（无法使用本地事件）

**修改后**:
- 如果不引入 spring-kafka 依赖 → 使用本地 Spring Events（正常启动）
- 如果引入 spring-kafka 依赖 → 自动检测 KafkaTemplate Bean，使用 Kafka 事件发布
- **符合设计原则**：依赖存在 → 用中间件；依赖不存在 → 用本地组件

---

## 验证步骤

### 验证1: 编译验证
```bash
cd "D:\Develop\code\mine\archetype\web-quick-start-domain"
mvn clean compile
```
**预期**: 编译成功，无错误

### 验证2: 依赖树验证
```bash
cd "D:\Develop\code\mine\archetype\web-quick-start-domain"
mvn dependency:tree -pl infrastructure
```
**预期**: spring-kafka 依赖在依赖树中标记为 optional

### 验证3: 应用启动验证（可选）
```bash
cd "D:\Develop\code\mine\archetype\web-quick-start-domain"
mvn spring-boot:run -pl start
```
**预期**: 启动成功，使用正确的事件发布器（Kafka 或 Spring Events）

---

## 重要说明

### 1. Prometheus 只能创建计划
**重要提示**: 我（Prometheus）是计划构建器，只能创建和修改 `.sisyphus/plans/` 和 `.sisyphus/notepads/` 目录下的 `.md` 文件。

**限制**:
- ❌ 不能直接修改 `infrastructure/pom.xml`
- ❌ 不能直接修改 `.java` 源代码文件
- ❌ 不能执行 Maven、Git 等命令
- ✅ 只能创建计划文件和补充说明

### 2. 执行方式

**推荐方式**: 使用 `/start-work` 命令
```bash
/start-work
```
Sisyphus（执行器）会：
1. 读取 `.sisyphus/plans/middleware-architecture-optimization.md` 计划
2. 按照计划中的 TODO 执行所有任务
3. 包括 Task 10: 添加 optional 标记
4. 提供进度跟踪和验证

### 3. 其他可选依赖

根据架构设计原则，以下依赖可能也需要考虑添加 optional 标记：
- `spring-boot-starter-data-redis`（如果 Redis 不是必需的）
- `spring-boot-starter-data-elasticsearch`（如果 Elasticsearch 不是必需的）

但根据当前计划，只需要处理 `spring-kafka` 依赖。

---

## 相关计划任务

这个补充任务与以下计划任务相关：
- Task 10: 添加 optional 标记（本补充任务）
- Task 11: 更新编码规范文档（已完成）

**Task 10 的依赖关系**:
- 无前置依赖（可以独立完成）
- Task 10 完成后，可以执行最终验证

---

## 总结

**问题**: 原计划中 Task 10 描述为"移除 optional 标记"，但实际应该为"添加 optional 标记"

**修正**: 已在主计划文件中更新 Task 10 的描述为"添加 optional 标记"

**执行**:
- 使用 `/start-work` 命令让 Sisyphus 执行所有计划任务
- Sisyphus 会自动处理 Task 10：添加 `<optional>true</optional>` 标记

**预期结果**: spring-kafka 依赖变为可选依赖，支持"自动检测 + 可选依赖"的架构设计

---

**文档创建时间**: 2026-01-25
**创建人**: Prometheus (计划构建器)
