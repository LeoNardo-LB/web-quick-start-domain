# CLAUDE.md - AI开发元指南

> **Purpose**: AI辅助开发的元指南，指导AI如何使用项目文档和开发流程
>
> **核心理念**: 安全第一、性能优先、可维护性至上
>
> **⚠️⚠️⚠️ 强制要求 ⚠️⚠️⚠️**
> **每次生成代码前，必须先阅读并严格遵守《业务代码编写规范.md》！**
>
> **常见违规（必须避免）：**
> - ❌ 使用 `@Data` 注解（**禁止**）
> - ❌ 使用 `@Service`、`@Component`、`@Repository` 类级别注解（**必须用@Configuration + @Bean**）
> - ❌ 使用 `@Autowired` 字段注入（**必须用构造器注入**）
> - ❌ 混用 `@RequiredArgsConstructor` 和手动构造器
> - ❌ 配置类放错模块（**配置类应在Bean所在模块**）

---

## 🤖 AI必读：违反规范的高风险场景

> **每次执行任务前，必须先检查以下风险等级！**

### 🔴 风险等级1（CRITICAL）：配置类违规

**触发条件**：

- 用户要求"创建配置类"、"配置Bean"、"注册Bean"
- 生成应用服务、领域服务、Repository实现类
- 创建事件处理器、监听器

**强制检查清单**：

```
□ 配置类位置是否在 start/src/main/java/org/smm/archetype/config/ ？
□ 配置类名是否按聚合根命名（OrderAggr → OrderConfigure）？
□ 是否使用 @Configuration + @Bean（而非@Service/@Component）？
□ 是否使用构造器注入（而非@Autowired字段注入）？
```

**错误示例**（绝对禁止）：

```java
// ❌ 错误：配置类在adapter模块
package org.smm.archetype.adapter._example.order.config;

@Configuration
public class OrderAdapterConfigure {}

// ❌ 错误：按层命名
@Configuration
public class OrderInfraConfigure {}

// ❌ 错误：使用@Service
@Service
public class OrderApplicationService {}
```

**正确示例**（必须遵循）：

```java
// ✅ 正确：start模块，按聚合根命名
package org.smm.archetype.config;

@Configuration
public class OrderConfigure {

    @Bean
    public OrderApplicationService orderApplicationService(...) {}

}
```

---

### 🟠 风险等级2（HIGH）：Lombok注解违规

**触发条件**：

- 生成实体类、DTO、值对象

**强制检查**：

- ❌ 绝不使用 @Data（必须手动实现 equals/hashCode/toString）
- ✅ 使用 @Getter/@Setter + @AllArgsConstructor 或手动构造器
- ✅ 值对象使用 @AllArgsConstructor + @FieldDefaults(makeFinal=true, level=PRIVATE)

---

### 🟡 风险等级3（MEDIUM）：_notes 文件夹使用

**触发条件**（满足任意一项即应创建笔记）：

- ✅ 任务涉及 **3个及以上文件** 的修改
- ✅ 任务预计耗时 **超过1小时**
- ✅ 用户提示词包含"设计"、"规划"、"重构"、"分析"关键词
- ✅ 任务需要 **跨越多个会话** 完成

**操作流程**：

```
1. 识别触发条件 → 2. 创建 _notes/current-task.md → 3. 记录进度 → 4. 完成后归档到 _notes/archive/
```

**笔记模板**：

```markdown
# Task: [任务标题]

**Created**: YYYY-MM-DD
**Status**: In Progress/Completed
**Estimated Complexity**: Low/Medium/High

## Overview

[1-2句话描述任务目标]

## Progress

- [ ] Step 1
- [x] Step 2 (completed YYYY-MM-DD)

## Findings

[重要发现、决策、阻塞点]

## Next Steps

[下一步行动]
```

---

### 🟢 风险等级4（NORMAL）：常规编码规范

**参考文档**：《业务代码编写规范.md》相关章节

---

## ⚡ 快速决策树

```
开始编码任务
    │
    ├─ 需要配置Bean？
    │    ├─ 是 → 检查配置类规范（位置、命名、注解）
    │    └─ 否 → 继续
    │
    ├─ 涉及3+文件或>1小时？
    │    ├─ 是 → 创建 _notes/current-task.md
    │    └─ 否 → 继续
    │
    ├─ 需要生成实体/DTO？
    │    ├─ 是 → 检查Lombok规范（禁止@Data）
    │    └─ 否 → 继续
    │
    └─ 遵循《业务代码编写规范.md》对应章节
```

---

# 🚨 快速参考

## 测试运行命令速查

```bash
# ===== 启动测试（最关键）=====
mvn test -Dtest=ApplicationStartupTests -pl start

# ===== 编译验证 =====
mvn clean compile

# ===== 单元测试 =====
mvn test

# ===== 生产环境运行（会一直运行）=====
mvn spring-boot:run -pl start
```

## 4步强制验证流程

| 步骤  | 命令                                                  | 验证目标     | ✅ 成功标志                               |
|-----|-----------------------------------------------------|----------|--------------------------------------|
| 1️⃣ | 编写单元测试                                              | 覆盖核心逻辑   | 测试类已创建                               |
| 2️⃣ | `mvn clean compile`                                 | 编译验证     | `BUILD SUCCESS`                      |
| 3️⃣ | `mvn test`                                          | 单元测试验证   | Tests run > 0, Failures: 0           |
| 4️⃣ | `mvn test -Dtest=ApplicationStartupTests -pl start` | **启动验证** | Tests run: 1, Failures: 0, Errors: 0 |

> **⚠️ 提醒**: 详细验证流程和故障排查见[代码AI生成工作流.md](AI生成代码工作流.md)

---

## 📋 目录

1. [文档导航](#文档导航)
2. [开发工作流](#开发工作流)
3. [_task文件夹系统](#_task文件夹系统)
4. [故障排查指南](#故障排查指南)
5. [代码质量标准](#代码质量标准)
6. [常见问题FAQ](#常见问题faq)

---

## 文档导航

### 📚 完整文档索引

| 文档                                                                                                  | 用途           | 目标读者       | 何时使用                | 重要性   |
|-----------------------------------------------------------------------------------------------------|--------------|------------|---------------------|-------|
| **[业务代码编写规范.md](业务代码编写规范.md)**                                                                      | **编码标准详细参考** | **开发者、AI** | **⭐ 写代码前必读，必须严格遵守** | ⭐⭐⭐⭐⭐ |
| [README.md](README.md)                                                                              | 项目概览和架构说明    | 所有人        | 了解项目整体架构            | ⭐⭐⭐⭐  |
| [代码AI生成工作流.md](AI生成代码工作流.md)                                                                        | 强制性代码生成流程    | AI、开发者     | **每次代码生成必须遵循**      | ⭐⭐⭐⭐⭐ |
| [domain/README.md](domain/src/main/java/org/smm/archetype/domain/README.md)                         | 领域层详细指南      | 开发者        | 开发领域模型时             | ⭐⭐⭐   |
| [app/README.md](app/src/main/java/org/smm/archetype/app/README.md)                                  | 应用层详细指南      | 开发者        | 开发应用服务时             | ⭐⭐⭐   |
| [adapter/README.md](adapter/src/main/java/org/smm/archetype/adapter/README.md)                      | 接口层详细指南      | 开发者        | 开发Controller时       | ⭐⭐⭐   |
| [infrastructure/README.md](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md) | 基础设施层详细指南    | 开发者        | 实现Repository时       | ⭐⭐⭐   |

### 🎯 按任务类型查找文档

#### 新增功能开发

1. **阅读**: [README.md](README.md#项目架构) - 了解四层架构
2. **阅读**: [业务代码编写规范.md](业务代码编写规范.md) - 了解编码规范
3. **阅读**: [代码AI生成工作流.md](AI生成代码工作流.md) - **遵循4步验证流程**
4. **参考**: [domain/README.md](domain/src/main/java/org/smm/archetype/domain/README.md) - 创建领域模型
5. **参考**: [app/README.md](app/src/main/java/org/smm/archetype/app/README.md) - 创建应用服务
6. **参考**: [infrastructure/README.md](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md) - 实现Repository

#### Bug修复

1. **定位**: 使用Grep/Glob工具查找相关代码
2. **阅读**: [业务代码编写规范.md](业务代码编写规范.md) - 确认修复方案符合规范
3. **遵循**: [代码AI生成工作流.md](AI生成代码工作流.md) - 完成4步验证
4. **验证**: 运行启动测试确保无副作用

#### 代码重构

1. **阅读**: [业务代码编写规范.md#8-代码设计原则](业务代码编写规范.md#8-代码设计原则) - 确保符合设计原则
2. **阅读**: [业务代码编写规范.md#84-接口实现分离模式](业务代码编写规范.md#84-接口实现分离模式三层架构) - 三层架构模式
3. **验证**: [代码AI生成工作流.md](AI生成代码工作流.md) - 确保启动测试通过

### 📖 各文档核心内容速查

#### README.md（项目概览）

- DDD符合度评分：8.6/10
- 四层架构说明
- 核心DDD概念简介
- 快速开始指南
- 技术栈信息

#### 业务代码编写规范.md（编码标准）

- **第1章**: 基础编码规范（格式、命名、Lombok、日志）
- **第2章**: 框架使用规范（SpringBoot Bean管理、Maven依赖）
- **第3章**: 领域建模规范（枚举、聚合根、实体、值对象）
- **第4章**: 数据持久化规范（DDL、字段类型、索引）
- **第5章**: 分层开发规范（Adapter、Application、Domain、Infrastructure）
- **第6章**: 设计原则（SOLID、接口-实现分离）
- **第7章**: 中间件接入规范（缓存、事件、通知）
- **第8章**: 禁止事项（代码、安全、性能、架构）

#### 代码AI生成工作流.md（强制流程）

- **步骤1**: 编写单元测试
- **步骤2**: 编译验证（`mvn clean compile`）
- **步骤3**: 执行单元测试（`mvn test`）
- **步骤4**: 启动验证（`mvn test -Dtest=ApplicationStartupTests -pl start`）⭐ **最关键**
- 附录A: 命令快速参考
- 附录B: 循环依赖解决示例
- 附录C: 常见错误示例

#### domain/README.md（领域层）

- 核心职责：领域模型、业务规则、领域服务、仓储接口
- 核心概念：AggregateRoot、Entity、ValueObject、DomainEvent、Repository、Specification
- 设计模式：DDD、Repository、Factory、Specification
- 架构约束：纯净业务逻辑、无外部依赖
- 最佳实践：业务逻辑封装、使用业务方法、事件发布

#### app/README.md（应用层）

- 核心职责：用例编排、事务管理、DTO转换
- 关键概念：ApplicationService、CQRS、事务边界
- 配置类：AppConfigure
- 通用模式：用例编排、DTO转换、事件发布
- 最佳实践：薄薄的一层、事务边界清晰

#### adapter/README.md（接口层）

- 核心职责：接收请求、参数验证、调用应用服务、返回响应
- 关键组件：Controller、EventListener、Schedule、Request/Response DTO
- 配置类：AdapterWebConfig、AdapterListenerConfig、AdapterScheduleConfig
- 通用模式：统一返回格式、异常处理、上下文填充
- 最佳实践：Controller保持简洁、DTO转换、参数验证

#### infrastructure/README.md（基础设施层）

- 核心职责：数据持久化、外部服务集成、消息中间件
- 关键组件：Repository实现、EventPublisher、CacheService、OssClient
- 配置类：EventConfigure、CacheConfigure、OssConfigure、ThreadPoolConfigure
- DO转换器：MapStruct配置
- 生成代码：MyBatis-Flex自动生成代码
- 最佳实践：Repository只做转换、中间件三层架构、条件装配

---

## 开发工作流

**详细工作流程**：[代码AI生成工作流.md](AI生成代码工作流.md)

本章节只保留快速参考，详细流程请参考工作流文档。

### 工作流概览

- **新增功能开发**：需求分析 → 确定代码位置 → 按规范生成代码 → 4步验证 → 完成
- **Bug修复**：定位问题 → 分析根因 → 设计修复方案 → 4步验证 → 完成
- **代码重构**：识别重构点 → 设计重构方案 → 逐步重构 → 4步验证 → 完成

**核心**：所有工作流都遵循4步强制验证流程（见上文"4步强制验证流程"）

---

## 文档边界说明

### 核心原则：每个文档各司其职，避免重复

#### CLAUDE.md（本文档）

**包含**：

- ✅ 文档导航系统（完整文档索引 + 按任务类型查找）
- ✅ 快速参考（测试运行命令速查 + 4步强制验证流程速查）
- ✅ _task文件夹系统完整文档
- ✅ 故障排查指南（编译/测试/启动失败的快速定位）
- ✅ 常见问题FAQ（文档选择、流程跳过、循环依赖等）
- ✅ 文档边界说明
- ✅ 使用场景映射

**不包含**：

- ❌ 详细编码规范 → [业务代码编写规范.md](业务代码编写规范.md)
- ❌ 验证流程细节 → [代码AI生成工作流.md](AI生成代码工作流.md)
- ❌ 测试生成规范 → [测试代码编写规范.md](测试代码编写规范.md)
- ❌ 架构详细说明 → [README.md](README.md) + 各模块README

**定位**：AI开发的入口和导航中心

---

#### README.md

**包含**：

- ✅ 项目概述
- ✅ DDD符合度评分（8.6/10）
- ✅ 快速开始（环境要求 + 编译 + 测试 + 运行）
- ✅ 项目架构（四层架构图 + 项目结构）
- ✅ 核心DDD概念简要介绍
- ✅ 技术栈信息

**不包含**：

- ❌ 详细开发指南 → [代码AI生成工作流.md](AI生成代码工作流.md)
- ❌ 完整工作流程 → [代码AI生成工作流.md](AI生成代码工作流.md)
- ❌ 故障排查信息 → [CLAUDE.md](CLAUDE.md)

**定位**：对外项目介绍，快速了解项目价值

---

#### 代码AI生成工作流.md

**包含**：

- ✅ 4步强制验证流程详细说明
- ✅ 每个步骤的命令、验证要点、成功标志
- ✅ **强制规则：每次生成业务代码后必须生成测试用例并保证通过**
- ✅ 代码质量检查清单
- ✅ 常见编译/测试/启动问题及解决方案
- ✅ 循环依赖解决示例
- ✅ 命令快速参考

**不包含**：

- ❌ 详细编码规范 → [业务代码编写规范.md](业务代码编写规范.md)
- ❌ 测试代码编写细节 → [测试代码编写规范.md](测试代码编写规范.md)
- ❌ 架构设计原则 → [业务代码编写规范.md](业务代码编写规范.md)

**定位**：强制执行的验证流程，确保代码质量

---

#### 业务代码编写规范.md

**包含**：

- ✅ 第1-8章：基础编码、框架使用、领域建模、数据持久化、分层开发、设计原则、中间件接入、禁止事项
- ✅ **第9章：测试规范（概述+引用）**
- ✅ 所有代码示例和最佳实践

**不包含**：

- ❌ 验证流程命令 → [代码AI生成工作流.md](AI生成代码工作流.md)
- ❌ 文档导航信息 → [CLAUDE.md](CLAUDE.md)
- ❌ 测试规范详细内容 → [测试代码编写规范.md](测试代码编写规范.md)

**定位**：编码标准权威参考，AI和开发者的编码手册

---

#### 测试代码编写规范.md

**包含**：

- ✅ 项目测试结构（test模块 + 基类）
- ✅ 单元测试规范（纯Mock，不启动Spring）
- ✅ 集成测试规范（Spring + H2 + DBUnit）
- ✅ 测试数据管理（DBUnit使用）
- ✅ 测试基类使用（UnitTestBase/IntegrationTestBase）
- ✅ 测试命名与组织规范
- ✅ 覆盖率要求（95%行，100%分支）
- ✅ 质量红线（100%通过率，0次DDL修改）

**不包含**：

- ❌ 业务代码编码规范 → [业务代码编写规范.md](业务代码编写规范.md)
- ❌ 验证流程细节 → [代码AI生成工作流.md](AI生成代码工作流.md)

**定位**：测试代码生成的唯一标准

---

#### 模块README（domain/app/adapter/infrastructure）

**包含**：

- ✅ 该层的职责
- ✅ 核心概念
- ✅ 开发模式
- ✅ 最佳实践

**不包含**：

- ❌ 跨层内容
- ❌ 通用编码规范 → [业务代码编写规范.md](业务代码编写规范.md)

**定位**：该层开发的详细指南

---

## 使用场景映射

### 场景1：首次接触项目

1. 先读 [README.md](README.md) - 了解项目整体架构
2. 再读 [CLAUDE.md](CLAUDE.md) - 了解文档体系

### 场景2：开始编写代码

1. 先读 [业务代码编写规范.md](业务代码编写规范.md) - 了解编码规范
2. 再读对应模块README - 了解该层开发模式

### 场景3：AI生成代码

1. 先读 [业务代码编写规范.md](业务代码编写规范.md) - 了解编码规范
2. 再读 [测试代码编写规范.md](测试代码编写规范.md) - 了解测试生成要求
3. 最后执行 [代码AI生成工作流.md](AI生成代码工作流.md) - 完成4步验证

### 场景4：验证失败

1. 先读 [代码AI生成工作流.md](AI生成代码工作流.md) - 查看故障排查决策树
2. 再读 [CLAUDE.md](CLAUDE.md) - 查看常见问题FAQ

### 场景5：开发特定层功能

1. 直接阅读对应模块README - 获取该层详细指南
2. 参考 [业务代码编写规范.md](业务代码编写规范.md) - 确保符合规范

---

## 笔记 [_notes](_notes)文件夹

### 位置

```
项目根目录/_notes
```

### 📁 用途

用于AI管理复杂、长期的任务笔记，开发者可以查看进度，但不应手动编辑。推荐每次遇到需求时，使用笔记进行任务管理。

### 📝 作用机制

1. **AI自主创建**: 当AI遇到复杂任务（如多文件修改、长期跟踪的问题、需要调研的方案等），自动创建对应的笔记文件
2. **持续更新**: AI在执行过程中不断更新进度、记录发现、标记阻塞点
3. **智能归档**: 任务完成后，AI自动将笔记移动到archive/目录并按日期命名
4. **开发者透明**: 开发者可以通过查看_notes/下的文件了解AI当前的工作状态和历史记录

### 🎯 适用场景

- **复杂功能开发**: 涉及多个模块、多个文件的大型功能开发
- **技术调研**: 需要对比多种方案、记录优缺点的场景
- **问题排查**: 追踪复杂bug、记录排查过程和最终解决方案
- **架构设计**: 记录设计决策、权衡考虑、未来扩展点
- **学习记录**: AI在处理新领域知识时的临时记录

### 🔧 AI使用建议

1. **自动判断**: 当任务预计超过1小时或涉及3个以上文件时，建议创建笔记
2. **定期更新**: 每完成一个重要步骤都要更新进度状态
3. **详细记录**: 记录关键决策点、遇到的问题、解决方案
4. **及时归档**: 任务完成后立即整理并归档相关笔记

> 这个系统旨在提高AI工作的透明度和连续性，开发者无需干预，但可以随时查看了解项目进展。

### 📂 结构

```
_notes/
├── current-task.md          # 当前任务笔记
├── todo-list.md             # 任务检查清单
├── research-notes.md        # 调查发现笔记
├── design-notes.md          # 设计决策笔记
├── progress-log.md          # 每日进度记录
└── archive/                 # 已完成任务归档
    ├── 2024-12-01-feature-x.md
    └── 2024-12-15-refactor-y.md
```

### 🔄 生命周期

1. **创建**: 当开始复杂任务时（多天、多文件）
2. **更新**: 每日更新进度、发现、阻塞点
3. **归档**: 任务完成后移到archive/并标注日期

### 👥 所有权

- **AI**: 创建、更新、管理笔记
- **开发者**: 只读查看，了解进度
- **原则**: AI自主决定何时创建/删除笔记

### 📝 模板

```markdown
# Task: [任务标题]

**Created**: 2024-12-15
**Status**: In Progress
**Estimated Complexity**: High

## Overview

[任务描述和目标]

## Approach

[实施方法和策略]

## Progress

- [ ] Step 1
- [ ] Step 2
- [x] Step 3 (completed 2024-12-15)

## Findings

[重要发现、决策、阻塞点]

## Next Steps

[下一步行动]
```

### 💡 使用示例

#### 创建任务笔记

```markdown
# Task: 文档重构

**Created**: 2026-01-10
**Status**: In Progress
**Estimated Complexity**: High

## Overview

将CLAUDE.md从73KB的单一文档重构为7个聚焦的文档，每个文档有明确的职责和目标读者。

## Approach

1. 创建代码编写规范.md（1,143行，18KB）
2. 创建代码AI生成工作流.md（666行，11KB）
3. 创建4个模块README（adapter、app、domain、infrastructure）
4. 简化主README.md
5. 转换CLAUDE.md为元指南

## Progress

- [x] 创建代码编写规范.md
- [x] 创建代码AI生成工作流.md
- [x] 创建domain/README.md
- [x] 创建adapter/README.md
- [x] 创建app/README.md
- [x] 创建infrastructure/README.md
- [x] 简化主README.md
- [ ] 转换CLAUDE.md为元指南
- [ ] 验证所有交叉引用

## Findings

- 成功减少约30%的冗余
- 文档职责划分清晰
- 交叉引用导航完善

## Next Steps

1. 完成CLAUDE.md转换
2. 验证所有链接
3. 更新进度文档
```

---

## 故障排查指南

### 🔍 编译失败

#### MapStruct转换器未生成

**症状**:

```
[ERROR] cannot find symbol
  symbol:   class XxxConverterImpl
```

**原因**: 注解处理器配置问题

**解决方案**:

1. 检查根POM的`maven-compiler-plugin`配置
2. 确保添加了`lombok-mapstruct-binding`
3. 运行`mvn clean compile`重新生成

**参考**: [代码AI生成工作流.md - 编译验证](AI生成代码工作流.md#步骤2编译验证)

#### 类型转换错误

**症状**:

```
[ERROR] XXXMapper.java:[34,17] Can't map property "XXX XXX"
```

**原因**: @Mapping注解配置错误

**解决方案**:

1. 使用`@Mapping(expression="java(...)")`
2. 或使用`@Mapping(target="...", ignore=true)`

**参考**: [代码AI生成工作流.md - 常见编译问题](AI生成代码工作流.md#常见编译问题)

### 🧪 单元测试失败

#### Mock未生效

**症状**: Mock对象的调用返回实际值而非期望值

**解决方案**:

1. 检查`when().thenReturn()`配置
2. 确保使用正确的mock实例
3. 验证Mock设置在调用之前完成

**参考**: [代码AI生成工作流.md - 测试失败处理](AI生成代码工作流.md#测试失败处理流程)

#### 断言失败

**症状**: 预期值与实际值不匹配

**解决方案**:

1. 检查业务逻辑是否正确
2. 验证测试用例是否合理
3. 查看失败的详细堆栈信息

**参考**: [代码AI生成工作流.md - 测试失败示例](AI生成代码工作流.md#附录c-常见错误示例)

### 🚀 启动测试失败

#### BeanCreationException

**症状**:

```
org.springframework.beans.factory.BeanCreationException
```

**原因**: Bean依赖注入失败

**解决方案**:

1. 检查配置类@Bean方法参数
2. 确保依赖完整
3. 查看详细堆栈定位失败Bean

**参考**: [代码AI生成工作流.md - 启动失败处理](AI生成代码工作流.md#步骤4主启动类启动验证-最关键)

#### 循环依赖

**症状**:

```
The dependencies of some of the beans in the application context form a cycle
```

**❌ 绝对禁止的解决方法**:

- 使用@Lazy注解
- 使用ObjectProvider延迟注入
- 使用ApplicationContext.getBean()依赖查找
- 使用@PostConstruct延迟初始化

**✅ 正确解决方法**:

- 重构代码、解耦依赖
- 跨配置类：使用构造器注入 + Optional
- 同配置类：使用@Bean方法参数注入

**参考**:

- [代码AI生成工作流.md - 循环依赖解决](AI生成代码工作流.md#⚠️-循环依赖解决原则)
- [业务代码编写规范.md - Bean管理](业务代码编写规范.md#26-springboot-bean管理规范)

#### NoSuchBeanDefinitionException

**症状**:
```
org.springframework.beans.factory.NoSuchBeanDefinitionException
No qualifying bean of type 'com.xxx.XxxService' available
```

**原因**: 配置类缺少@Bean方法

**解决方案**:

1. 检查配置类中是否有对应的@Bean方法
2. 确保条件装配（@ConditionalOnBean）满足
3. 验证依赖Bean已创建

**参考**: [代码AI生成工作流.md - 常见启动失败问题](AI生成代码工作流.md#常见启动失败问题)

---

## 代码质量标准

**详细质量检查清单**：[代码AI生成工作流.md - 代码质量检查清单](AI生成代码工作流.md#代码质量检查清单)

### 快速参考

- ✅ **编译通过**: `mvn clean compile`
- ✅ **单元测试通过**: `mvn test` (通过率100%)
- ✅ **集成测试通过**: `mvn test -Dtest=*IntegrationTest -pl test` (通过率100%)
- ✅ **启动测试通过**: `mvn test -Dtest=ApplicationStartupTests -pl start`
- ✅ **测试覆盖率**: 行≥95%，分支100%

---

## 常见问题FAQ

### Q1: 如何选择文档？

**A**: 根据任务类型选择：

- **了解项目**: README.md
- **编写代码**: 业务代码编写规范.md
- **生成代码**: 代码AI生成工作流.md（**必须遵循**）
- **开发领域模型**: domain/README.md
- **开发应用服务**: app/README.md
- **开发Controller**: adapter/README.md
- **实现Repository**: infrastructure/README.md

### Q2: 4步验证流程可以跳过吗？

**A**: **绝对不可以**。这是强制流程，确保代码质量：

1. 单元测试确保逻辑正确
2. 编译验证确保无语法错误
3. 单元测试验证确保测试覆盖
4. 启动验证确保应用可以正常运行

**详细流程**: [代码AI生成工作流.md](AI生成代码工作流.md)

### Q3: 如何解决循环依赖？

**A**: 必须通过重构代码来解决：

- ✅ 重构架构、解耦依赖
- ✅ 提取公共接口
- ✅ 直接注入具体实现
- ✅ 使用事件驱动解耦

**❌ 绝对禁止**:

- 使用@Lazy注解
- 使用ObjectProvider延迟注入
- 使用ApplicationContext.getBean()依赖查找
- 使用@PostConstruct延迟初始化

**详细说明**: [代码AI生成工作流.md - 循环依赖解决](AI生成代码工作流.md#⚠️-循环依赖解决原则)

### Q4: 何时使用三层架构（接口-抽象基类-实现）？

**A**:
- ✅ 通用服务（缓存、队列、存储）
- ✅ 可能有多种实现的服务
- ✅ 需要统一处理流程的服务
- ❌ 简单业务逻辑
- ❌ 一次性代码

**详细说明**: [业务代码编写规范.md - 接口实现分离](业务代码编写规范.md#84-接口实现分离模式三层架构)

### Q5: _task文件夹由谁管理？

**A**:

- **AI**: 创建、更新、删除笔记
- **开发者**: 只读查看，了解进度
- **原则**: AI自主决定，开发者不干预

**详细说明**: 本文档[#_task文件夹系统](#_task文件夹系统)章节

---

**文档版本**: v5.0
**最后更新**: 2026-01-10
**维护者**: Leonardo

**v5.0 更新说明**:

- **重大重构**: 将CLAUDE.md转换为AI元指南
- **移除内容**: 所有详细编码规范迁移到[业务代码编写规范.md](业务代码编写规范.md)
- **移除内容**: 所有架构详情迁移到[README.md](README.md)
- **新增**: 文档导航系统
- **新增**: 开发工作流（新增功能、Bug修复、重构）
- **新增**: _task文件夹系统文档
- **增强**: 故障排查指南
- **优化**: 交叉引用到所有文档

**相关文档**:

- [README.md](README.md) - 项目概览
- [业务代码编写规范.md](业务代码编写规范.md) - 编码标准
- [代码AI生成工作流.md](AI生成代码工作流.md) - 代码生成流程
- [domain/README.md](domain/src/main/java/org/smm/archetype/domain/README.md) - 领域层
- [app/README.md](app/src/main/java/org/smm/archetype/app/README.md) - 应用层
- [adapter/README.md](adapter/src/main/java/org/smm/archetype/adapter/README.md) - 接口层
- [infrastructure/README.md](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md) - 基础设施层
