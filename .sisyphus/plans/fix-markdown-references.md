# Markdown文档引用全面优化

## Context

### Original Request
对项目中所有 Markdown 文档的引用进行整体搜索、重构与优化，建立连接或删除连接，确保所有 .md 文档都能正确引用。

### Interview Summary
**用户确认的处理方式：**
- 删除所有死链引用（domain/README.md, app/README.md, adapter/README.md, infrastructure/README.md, test/README.md）
- 删除 CLAUDE.md 引用（不创建文件）
- archetype_quickstart.md 的引用根据文章语义决定（修正为 `_docs/specification/快速上手指南.md`）

**审计发现：**
- 28处死链引用（不存在的文件）
- 5处路径错误引用
- 文档结构说明与实际文件系统不一致

**受影响的文件：**
1. README.md（22处引用需要修正/删除）
2. _docs/business/README.md（6处引用需要删除）

---

## Work Objectives

### Core Objective
清理所有 Markdown 文档中的死链引用和错误路径，确保所有文档链接都能正确访问。

### Concrete Deliverables
- README.md：删除/修正所有死链和路径错误
- _docs/business/README.md：删除所有死链引用
- 确保所有引用指向存在的文件
- 保持文档的可读性和完整性

### Definition of Done
- [ ] README.md 中无不存在的文件引用
- [ ] _docs/business/README.md 中无不存在的文件引用
- [ ] 所有文档链接指向正确路径
- [ ] 文档格式正确，无孤立的格式标记
- [ ] 文档结构说明与实际文件系统一致

### Must Have
- 删除所有死链引用（CLAUDE.md, domain/README.md 等）
- 修正错误的路径引用
- 保持文档结构的完整性

### Must NOT Have (Guardrails)
- 不得删除有效的文档引用
- 不得破坏文档的可读性
- 不得遗漏任何死链引用

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: NO (Manual-only)
- **Framework**: none

### Manual QA Only

由于是文本清理工作，采用手动验证方式。

**For markdown 文档修改：**
- [ ] 使用 grep 验证无死链文件名：
  - `grep -r "CLAUDE\.md" --include="*.md" . | grep -v ".sisyphus"` → 无匹配
  - `grep -r "domain/README\.md" --include="*.md" . | grep -v ".sisyphus"` → 无匹配
  - `grep -r "app/README\.md" --include="*.md" . | grep -v ".sisyphus"` → 无匹配
  - `grep -r "adapter/README\.md" --include="*.md" . | grep -v ".sisyphus"` → 无匹配
  - `grep -r "infrastructure/README\.md" --include="*.md" . | grep -v ".sisyphus"` → 无匹配
  - `grep -r "test/README\.md" --include="*.md" . | grep -v ".sisyphus"` → 无匹配
- [ ] 验证路径修正正确：
  - `grep "_docs/验证流程指南.md" README.md` → 无匹配（应改为 specification/）
  - `grep "_docs/快速上手指南.md" README.md` → 无匹配（应改为 specification/）
- [ ] 检查文档格式：
  - Markdown 语法正确
  - 无孤立的列表项或分隔符

---

## Task Flow

```
Task 1 → Task 2 → Task 3 → Task 4 → Task 5
```

## Parallelization

无并行任务，所有任务顺序执行。

| Task | Depends On | Reason |
|------|------------|--------|
| 1 | - | 修正路径错误（保留部分） |
| 2 | 1 | 删除死链（domain/README.md 等） |
| 3 | 2 | 删除 CLAUDE.md 引用 |
| 4 | 3 | 删除目录结构中的不存在的文件说明 |
| 5 | 4 | 同样处理 _docs/business/README.md |
| 6 | 5 | 全面验证 |

---

## TODOs

- [x] 1. 修正 README.md 中的错误路径引用（保留部分）

  **What to do**:
  - 将 `_docs/验证流程指南.md` 改为 `_docs/specification/验证流程指南.md`（第81、484行）
  - 将 `_docs/快速上手指南.md` 改为 `_docs/specification/快速上手指南.md`（第513行）
  - 将 `业务代码编写规范.md` 改为 `_docs/specification/业务代码编写规范.md`（第515、620行）

  **Must NOT do**:
  - 不得修改其他有效的引用
  - 不得删除保留的引用

  **Parallelizable**: NO

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - `README.md:60` - 正确的引用格式：`[快速上手指南](_docs/specification/快速上手指南.md)`
  - `README.md:494-498` - 规范文档的正确引用格式

  **Why Each Reference Matters**:
  - 第60行等位置已经有正确的引用格式，作为参考标准
  - 需要保持一致的引用路径风格

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] 第81行修改：
    ```
    原文：详细流程：[验证流程指南](_docs/验证流程指南.md)
    改为：详细流程：[验证流程指南](_docs/specification/验证流程指南.md)
    ```
  - [ ] 第484行修改：
    ```
    原文：详细流程：[验证流程指南](_docs/验证流程指南.md)
    改为：详细流程：[验证流程指南](_docs/specification/验证流程指南.md)
    ```
  - [ ] 第513行修改：
    ```
    原文：1. 阅读 [快速上手指南](_docs/快速上手指南.md) - 快速上手
    改为：1. 阅读 [快速上手指南](_docs/specification/快速上手指南.md) - 快速上手
    ```
  - [ ] 第515行修改：
    ```
    原文：3. 阅读 [业务代码编写规范](_docs/业务代码编写规范.md) - 编码规范
    改为：3. 阅读 [业务代码编写规范](_docs/specification/业务代码编写规范.md) - 编码规范
    ```
  - [ ] 第547行修改：
    ```
    原文：参考：[业务代码编写规范](_docs/业务代码编写规范.md) 2.6节
    改为：参考：[业务代码编写规范](_docs/specification/业务代码编写规范.md) 2.6节
    ```
  - [ ] 第579行修改：
    ```
    原文：参考：[业务代码编写规范](_docs/业务代码编写规范.md) 2.6节
    改为：参考：[业务代码编写规范](_docs/specification/业务代码编写规范.md) 2.6节
    ```
  - [ ] 第620行修改：
    ```
    原文：2. 阅读 [业务代码编写规范.md](业务代码编写规范.md) 学习编码规范
    改为：2. 阅读 [业务代码编写规范](_docs/specification/业务代码编写规范.md) 学习编码规范
    ```

  **Evidence Required**:
  - [ ] grep 验证：
    ```
    grep "_docs/验证流程指南.md" README.md
    Expected: 无匹配结果
    grep "_docs/快速上手指南.md" README.md
    Expected: 无匹配结果
    grep "业务代码编写规范.md" README.md | grep -v "specification"
    Expected: 无匹配结果
    ```
  - [ ] 修改后相关行的内容

  **Commit**: NO (groups with 6)

- [x] 2. 删除 README.md 中对模块 README 的死链引用

  **What to do**:
  - 删除所有 `domain/README.md`, `app/README.md`, `adapter/README.md`, `infrastructure/README.md`, `test/README.md` 的引用

  **具体位置**：
  - 第144行：删除整行链接
  - 第389行：删除链接
  - 第412行：删除链接
  - 第430行：删除链接
  - 第447行：删除链接
  - 第469行：删除链接
  - 第500-504行：删除表格中的5行
  - 第514行：删除整行
  - 第564行：删除整行

  **Must NOT do**:
  - 不得删除其他有效的文档引用
  - 不得破坏文档结构

  **Parallelizable**: NO (depends on 1)

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - `README.md:494-498` - 表格的正确格式
  - `README.md:562-565` - 列表的正确格式

  **Why Each Reference Matters**:
  - 删除行后需要保持表格和列表的格式正确

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] 第144行删除：
    ```
    原文：详细指南：[domain/README.md](domain/README.md) | [app/README.md](app/README.md) | [adapter/README.md](adapter/README.md) | [infrastructure/README.md](infrastructure/README.md)
    改为：（删除整行）
    ```
  - [ ] 第389行删除：
    ```
    原文：参考：[domain/README.md](domain/README.md)
    改为：参考：[领域层开发](#领域层)  # 或删除整行
    ```
  - [ ] 第412行删除：
    ```
    原文：参考：[app/README.md](app/README.md)
    改为：（删除整行）
    ```
  - [ ] 第430行删除：
    ```
    原文：参考：[adapter/README.md](adapter/README.md)
    改为：（删除整行）
    ```
  - [ ] 第447行删除：
    ```
    原文：参考：[infrastructure/README.md](infrastructure/README.md)
    改为：（删除整行）
    ```
  - [ ] 第469行删除：
    ```
    原文：参考：[test/README.md](test/README.md)
    改为：（删除整行）
    ```
  - [ ] 第500-504行删除（表格中的5行）：
    ```
    删除以下行：
    | **[domain/README.md](domain/README.md)** | 领域层开发 | 开发者 |
    | **[app/README.md](app/README.md)** | 应用层开发 | 开发者 |
    | **[adapter/README.md](adapter/README.md)** | 接口层开发 | 开发者 |
    | **[infrastructure/README.md](infrastructure/README.md)** | 基础设施层开发 | 开发者 |
    | **[test/README.md](test/README.md)** | 测试开发 | 开发者 |
    ```
  - [ ] 第513-515行调整：
    ```
    原文：
    1. 阅读 [快速上手指南](_docs/specification/快速上手指南.md) - 快速上手
    2. 阅读 [domain/README.md](domain/README.md) - 领域层开发
    3. 阅读 [业务代码编写规范](_docs/specification/业务代码编写规范.md) - 编码规范

    改为：
    1. 阅读 [快速上手指南](_docs/specification/快速上手指南.md) - 快速上手
    2. 阅读 [业务代码编写规范](_docs/specification/业务代码编写规范.md) - 编码规范
    3. 参考订单示例代码 - 学习DDD概念
    ```
  - [ ] 第562-565行调整：
    ```
    原文：
    1. [快速上手指南](_docs/specification/快速上手指南.md) - 快速上手
    2. [README.md](README.md) - 项目架构
    3. [domain/README.md](domain/README.md) - 领域层开发
    4. 订单示例代码 - 75个类，涵盖所有DDD概念

    改为：
    1. [快速上手指南](_docs/specification/快速上手指南.md) - 快速上手
    2. [README.md](README.md) - 项目架构
    3. 订单示例代码 - 75个类，涵盖所有DDD概念
    ```

  **Evidence Required**:
  - [ ] grep 验证：
    ```
    grep "domain/README\.md" README.md
    Expected: 无匹配结果
    grep "app/README\.md" README.md
    Expected: 无匹配结果
    grep "adapter/README\.md" README.md
    Expected: 无匹配结果
    grep "infrastructure/README\.md" README.md
    Expected: 无匹配结果
    grep "test/README\.md" README.md
    Expected: 无匹配结果
    ```

  **Commit**: NO (groups with 6)

- [x] 3. 删除 README.md 中对 CLAUDE.md 的引用

  **What to do**:
  - 删除所有 `CLAUDE.md` 的引用

  **具体位置**：
  - 第360行：删除目录结构中的 CLAUDE.md
  - 第499行：删除表格中的 CLAUDE.md 行

  **Must NOT do**:
  - 不得删除其他有效的文档引用
  - 不得破坏文档结构

  **Parallelizable**: NO (depends on 2)

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - `README.md:359-361` - 目录结构的正确格式

  **Why Each Reference Matters**:
  - 删除后需要保持目录结构的正确性

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] 第360行删除：
    ```
    原文：
    ├── README.md            # 本文件
    ├── CLAUDE.md            # AI开发指南
    └── pom.xml              # Maven配置

    改为：
    ├── README.md            # 本文件
    └── pom.xml              # Maven配置
    ```
  - [ ] 第499行删除（表格行）：
    ```
    删除以下行：
    | **[CLAUDE.md](CLAUDE.md)** | AI开发指南 | AI、开发者 |
    ```

  **Evidence Required**:
  - [ ] grep 验证：
    ```
    grep "CLAUDE\.md" README.md
    Expected: 无匹配结果
    ```

  **Commit**: NO (groups with 6)

- [x] 4. 调整 README.md 中的其他相关内容

  **What to do**:
  - 根据删除的引用，调整文档的上下文内容
  - 确保文档的可读性和完整性

  **具体调整**：
  - 第144行删除后，检查是否有影响上下文的表述
  - 第389-469行删除后，检查"开发指南"部分的完整性
  - 第517-520行（架构师部分）的"阅读各层README"需要删除或调整

  **Must NOT do**:
  - 不得过度修改文档内容
  - 只删除明确引用不存在文件的部分

  **Parallelizable**: NO (depends on 3)

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - `README.md:517-520` - 当前内容作为参考

  **Why Each Reference Matters**:
  - 需要保持文档的逻辑连贯性

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] 第517-520行调整：
    ```
    原文：
    #### 架构师
    1. 阅读 [README.md](README.md) - 项目架构
    2. 阅读各层README - 架构设计
    3. 查看订单模块示例 - DDD实战

    改为：
    #### 架构师
    1. 阅读 [README.md](README.md) - 项目架构
    2. 查看订单模块示例 - DDD实战
    ```
  - [ ] 检查文档其他相关部分的逻辑连贯性

  **Evidence Required**:
  - [ ] 检查修改后的文档，确保无孤立的句子或段落

  **Commit**: NO (groups with 6)

- [x] 5. 删除 _docs/business/README.md 中的死链引用

  **What to do**:
  - 删除所有对不存在文件的引用
  - 删除 CLAUDE.md 引用（第100行）
  - 删除模块 README 引用（第103-107行）

  **Must NOT do**:
  - 不得删除其他有效的文档引用
  - 不得破坏文档结构

  **Parallelizable**: NO (depends on 4)

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - `_docs/business/README.md:92-96` - 有效引用的正确格式
  - `_docs/business/README.md:99` - 项目根文档的正确引用格式

  **Why Each Reference Matters**:
  - 保持文档结构的完整性

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] 第100行删除：
    ```
    原文：- **[CLAUDE.md](../../CLAUDE.md)** - AI开发元指南
    改为：（删除整行）
    ```
  - [ ] 第103-107行删除（5行）：
    ```
    删除以下行：
    - **[domain/README.md](../../domain/README.md)** - 领域层开发
    - **[app/README.md](../../app/README.md)** - 应用层开发
    - **[adapter/README.md](../../adapter/README.md)** - 接口层开发
    - **[infrastructure/README.md](../../infrastructure/README.md)** - 基础设施层开发
    - **[test/README.md](../../test/README.md)** - 测试开发
    ```

  **Evidence Required**:
  - [ ] grep 验证：
    ```
    grep "CLAUDE\.md" _docs/business/README.md
    Expected: 无匹配结果
    grep "domain/README\.md" _docs/business/README.md
    Expected: 无匹配结果
    grep "app/README\.md" _docs/business/README.md
    Expected: 无匹配结果
    grep "adapter/README\.md" _docs/business/README.md
    Expected: 无匹配结果
    grep "infrastructure/README\.md" _docs/business/README.md
    Expected: 无匹配结果
    grep "test/README\.md" _docs/business/README.md
    Expected: 无匹配结果
    ```

  **Commit**: NO (groups with 6)

- [x] 6. 全面验证所有修改

  **What to do**:
  - 全局搜索确认所有死链已清理
  - 检查所有文档链接的正确性
  - 验证文档格式和结构

  **Must NOT do**:
  - 不得遗漏任何死链引用

  **Parallelizable**: NO (depends on 1, 2, 3, 4, 5)

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - 无特定模式，使用 grep 进行全局验证

  **Why Each Reference Matters**:
  - 确保彻底清理所有引用问题

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] 验证 README.md 中无不存在的文件引用：
    ```
    grep -E "(CLAUDE\.md|domain/README\.md|app/README\.md|adapter/README\.md|infrastructure/README\.md|test/README\.md)" README.md
    Expected: 无匹配结果
    ```
  - [ ] 验证 README.md 中无错误的路径引用：
    ```
    grep "_docs/验证流程指南.md" README.md
    Expected: 无匹配结果
    grep "_docs/快速上手指南.md" README.md
    Expected: 无匹配结果
    grep "业务代码编写规范.md" README.md | grep -v "specification"
    Expected: 无匹配结果
    ```
  - [ ] 验证 _docs/business/README.md 中无不存在的文件引用：
    ```
    grep -E "(CLAUDE\.md|domain/README\.md|app/README\.md|adapter/README\.md|infrastructure/README\.md|test/README\.md)" _docs/business/README.md
    Expected: 无匹配结果
    ```
  - [ ] 检查文档格式：
    - 打开 README.md，检查表格、列表格式是否正确
    - 打开 _docs/business/README.md，检查列表格式是否正确
    - 确认无孤立的格式标记
  - [ ] 验证文档链接有效性：
    ```
    检查 README.md 中所有 markdown 链接是否指向存在的文件
    ```
  - [ ] 文档结构说明与实际一致：
    ```
    检查 README.md 中目录结构部分是否与实际文件系统一致
    ```

  **Evidence Required**:
  - [ ] 所有 grep 命令的输出（显示无匹配）
  - [ ] README.md 和 _docs/business/README.md 的最终内容检查
  - [ ] 文档链接有效性验证结果

  **Commit**: YES
  - Message: `docs: fix all dead links and incorrect paths in markdown documentation`
  - Files: README.md, _docs/business/README.md
  - Pre-commit: 执行验证脚本

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 6 | `docs: fix all dead links and incorrect paths in markdown documentation` | README.md, _docs/business/README.md | 全局 grep 验证无死链 |

---

## Success Criteria

### Verification Commands
```bash
# 验证无不存在的文件引用
grep -r "CLAUDE\.md" --include="*.md" . | grep -v ".sisyphus"  # Expected: 无匹配
grep -r "domain/README\.md" --include="*.md" . | grep -v ".sisyphus"  # Expected: 无匹配
grep -r "app/README\.md" --include="*.md" . | grep -v ".sisyphus"  # Expected: 无匹配
grep -r "adapter/README\.md" --include="*.md" . | grep -v ".sisyphus"  # Expected: 无匹配
grep -r "infrastructure/README\.md" --include="*.md" . | grep -v ".sisyphus"  # Expected: 无匹配
grep -r "test/README\.md" --include="*.md" . | grep -v ".sisyphus"  # Expected: 无匹配

# 验证无错误的路径引用
grep "_docs/验证流程指南.md" README.md  # Expected: 无匹配
grep "_docs/快速上手指南.md" README.md  # Expected: 无匹配
```

### Final Checklist
- [ ] README.md 中无不存在的文件引用
- [ ] _docs/business/README.md 中无不存在的文件引用
- [ ] 所有错误的路径引用已修正
- [ ] 文档格式正确，无孤立标记
- [ ] 文档链接全部有效
- [ ] 文档结构说明与实际一致
