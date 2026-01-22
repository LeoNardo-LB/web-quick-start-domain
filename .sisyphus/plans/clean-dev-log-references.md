# 清理 dev_log 失效引用

## Context

### Original Request
用户之前删除了项目中的 `_docs/dev_log` 文件夹，需要清理其他 markdown 文档中指向该文件夹的超链接和引用。

### Interview Summary
**用户确认的处理方式：**
- archetype_quickstart.md 中的 dev_log 引用：直接删除引用
- _docs/specification/README.md 中的文档导航链接：删除整个链接项

**发现的引用位置：**
1. **archetype_quickstart.md** (第196行和第208行)
2. **_docs/specification/README.md** (第120行)

---

## Work Objectives

### Core Objective
清理所有 markdown 文档中对已删除目录 `_docs/dev_log` 的引用。

### Concrete Deliverables
- archetype_quickstart.md：删除 dev_log 相关配置和说明
- _docs/specification/README.md：删除 [AI工作记录] 导航链接项

### Definition of Done
- [ ] archetype_quickstart.md 中无 dev_log 引用
- [ ] _docs/specification/README.md 中无 dev_log 链接
- [ ] 文档格式正确，无语法错误

### Must Have
- 删除所有 dev_log 相关引用
- 保持文档格式整洁

### Must NOT Have (Guardrails)
- 不得删除其他有效的配置项
- 不得破坏文档结构

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: NO (Manual-only)
- **Framework**: none

### Manual QA Only

由于是简单的文本清理工作，采用手动验证方式。

**For markdown 文档修改：**
- [ ] Using grep 命令验证：
  - Command: `grep -r "dev_log" --include="*.md"`
  - Expected: 无匹配结果（清理干净）
- [ ] 查看修改后的文件内容：
  - archetype_quickstart.md 第196-208行：确认 dev_log 相关内容已删除
  - _docs/specification/README.md 第120行：确认 [AI工作记录] 链接项已删除
- [ ] 验证文档格式：
  - Markdown 语法正确（列表、链接等）
  - 无孤立的格式标记

---

## Task Flow

```
Task 1 → Task 2 → Task 3
```

## Parallelization

无并行任务，所有任务顺序执行。

| Task | Depends On | Reason |
|------|------------|--------|
| 1 | - | 首要任务 |
| 2 | 1 | 顺序处理 |
| 3 | 2 | 验证修改 |

---

## TODOs

- [ ] 1. 修改 archetype_quickstart.md - 删除 dev_log 配置引用

  **What to do**:
  - 删除第196行 `excludePatterns` 配置中的 `dev_log/**` 模式
  - 删除第208行关于 `dev_log/**` 的说明项
  - 保持配置格式的一致性

  **Must NOT do**:
  - 不得删除其他有效的排除模式项
  - 不得修改其他配置说明

  **Parallelizable**: NO

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - `archetype_quickstart.md:191-209` - 排除配置格式（properties代码块 + 列表说明）

  **Why Each Reference Matters**:
  - 需要保持原有的列表格式和缩进风格
  - 其他配置项的格式要保持一致

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] Using text editor:
    - 打开文件: `archetype_quickstart.md`
    - 定位到第196行，找到 `excludePatterns=...`
    - 删除其中的 `,dev_log/**` 部分（注意删除前面的逗号）
    - 定位到第208行，找到 `- `dev_log/**` - 开发日志目录` 行
    - 删除整行
    - 保存文件
  - [ ] 验证删除结果：
    ```
    grep "dev_log" archetype_quickstart.md
    Expected: 无匹配结果
    ```
  - [ ] 检查配置格式：
    ```
    查看 archetype_quickstart.md 第196行附近
    Expected: properties配置格式正确，其他排除模式项格式一致
    ```

  **Evidence Required**:
  - [ ] grep 命令输出截图（无 dev_log 匹配）
  - [ ] 修改后第196行和第208行的内容

  **Commit**: NO (groups with 3)

- [ ] 2. 修改 _docs/specification/README.md - 删除文档导航链接

  **What to do**:
  - 删除第120行的整个 [AI工作记录] 链接项
  - 保持其他链接项的格式

  **Must NOT do**:
  - 不得删除其他有效的文档导航链接
  - 不得破坏文档的结构

  **Parallelizable**: NO (depends on 1)

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - `_docs/specification/README.md:116-122` - 相关文档导航格式（markdown列表 + 链接）

  **Why Each Reference Matters**:
  - 需要保持 markdown 列表格式
  - 其他链接项的格式要保持一致

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] Using text editor:
    - 打开文件: `_docs/specification/README.md`
    - 定位到第120行，找到 `- **[AI工作记录]`../dev_log/README.md`** - AI辅助开发日志目录`
    - 删除整行
    - 保存文件
  - [ ] 验证删除结果：
    ```
    grep "dev_log" _docs/specification/README.md
    Expected: 无匹配结果
    ```
  - [ ] 检查文档格式：
    ```
    查看 _docs/specification/README.md 第116-122行附近
    Expected: markdown列表格式正确，其他链接项正常
    ```

  **Evidence Required**:
  - [ ] grep 命令输出截图（无 dev_log 匹配）
  - [ ] 修改后第116-122行的内容

  **Commit**: NO (groups with 3)

- [ ] 3. 验证所有修改完成

  **What to do**:
  - 全局搜索确认所有 dev_log 引用已清理
  - 检查修改后的文档格式正确性
  - 确认文档可以正常阅读和渲染

  **Must NOT do**:
  - 不得遗漏任何 dev_log 引用

  **Parallelizable**: NO (depends on 1, 2)

  **References** (CRITICAL - Be Exhaustive):

  **Pattern References** (existing code to follow):
  - 无特定模式，使用 grep 进行全局搜索

  **Why Each Reference Matters**:
  - 确保彻底清理所有引用

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  - [ ] 全局搜索验证：
    ```
    grep -r "dev_log" --include="*.md" .
    Expected: 无匹配结果
    ```
  - [ ] 检查修改后的文档：
    - 打开 `archetype_quickstart.md`，查看第196-208行，确认删除正确
    - 打开 `_docs/specification/README.md`，查看第116-122行，确认删除正确
  - [ ] 验证 markdown 格式：
    - 确认列表项对齐正确
    - 确认没有孤立的格式标记（如多余的 `-` 符号）
    - 确认 markdown 语法正确

  **Evidence Required**:
  - [ ] grep 命令完整输出（显示无匹配）
  - [ ] archetype_quickstart.md 修改前后的对比（可选）
  - [ ] _docs/specification/README.md 修改前后的对比（可选）

  **Commit**: YES
  - Message: `docs: remove dead references to deleted dev_log directory`
  - Files: `archetype_quickstart.md`, `_docs/specification/README.md`
  - Pre-commit: `grep -r "dev_log" --include="*.md" .`

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 3 | `docs: remove dead references to deleted dev_log directory` | archetype_quickstart.md, _docs/specification/README.md | grep -r "dev_log" --include="*.md" . |

---

## Success Criteria

### Verification Commands
```bash
grep -r "dev_log" --include="*.md" .  # Expected: 无匹配结果
```

### Final Checklist
- [ ] archetype_quickstart.md 中无 dev_log 引用
- [ ] _docs/specification/README.md 中无 dev_log 引用
- [ ] 所有 markdown 文档格式正确
- [ ] 无孤立的格式标记
