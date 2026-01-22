# Documentation Cleanup and Restructuring Plan

## Context

### Original Request
用户要求处理三个文档问题：
1. 删除快速上手指南.md（与README.md的快速开始重复）
2. 将规范文件从 `_docs/` 移回 `_docs/specification/`（当前位置错误）
3. 处理 dev_log 的引用混乱问题

### Interview Summary
**Key Discussions**:
- **快速上手指南必要性**: 用户确认删除，因为根目录 README.md 已包含完整的快速开始内容（第58-97行），两者高度重复
- **规范文件位置**: 用户确认必须移回 `_docs/specification/` 目录
- **dev_log 混淆**: 根目录 `dev_log/` 已删除，但 `_docs/dev_log/` 仍然存在

**Research Findings**:
- 快速上手指南在4个文件中被引用14次
- 5个规范文件错误地放在 `_docs/` 根目录，应该在 `specification/` 子目录
- `CLAUDE.md` 仍有8处对已删除的根目录 `dev_log/` 的引用
- 文档结构描述与实际文件系统不一致

---

## Work Objectives

### Core Objective
整理文档结构，确保规范文件位置正确，删除重复内容，修复失效引用

### Concrete Deliverables
- 删除 `快速上手指南.md` 文件
- 移动4个规范文件到 `_docs/specification/` 目录
- 更新所有文件引用路径
- 清理 dev_log 相关引用

### Definition of Done
- [ ] `快速上手指南.md` 已删除
- [ ] 4个规范文件已移动到 `_docs/specification/`
- [ ] 所有引用路径已更新（指向正确的规范文件位置）
- [ ] dev_log 引用已清理或修复
- [ ] 文档结构描述与实际文件系统一致
- [ ] 编译验证通过：`mvn clean compile`

### Must Have
- 规范文件必须在 `_docs/specification/` 目录
- 所有引用路径必须正确
- 不能有失效的链接

### Must NOT Have (Guardrails)
- 不能保留重复的快速上手指南
- 不能有指向不存在文件的引用
- 不能修改业务代码（只修改文档）

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO (文档修改，无需测试)
- **User wants tests**: NO (文档修改不需要测试)
- **Framework**: none

### Manual QA Only

所有验证步骤为手动命令行验证：

| 任务类型 | 验证工具 | 过程 |
|---------|---------|------|
| 文档修改 | Bash 命令 | 检查文件存在性、引用正确性 |

**验证命令**：
```bash
# 验证文件移动
ls -la _docs/specification/ | grep "规范\|指南"
ls -la _docs/ | grep -v "^d" | grep "\.md$"  # 应该只剩下规范文件以外的文件

# 验证文件删除
! [ -f _docs/快速上手指南.md ]  # 应该失败（文件不存在）

# 验证引用更新
grep -r "快速上手指南" --include="*.md"  # 应该无结果
grep -r "_docs/业务代码编写规范.md" --include="*.md"  # 应该无结果（应该指向 _docs/specification/）
grep -r "_docs/specification/业务代码编写规范.md" --include="*.md"  # 应该有结果
```

---

## Task Flow

```
Task 1 → Task 2 → Task 3
         ↘ Task 4 (parallel)
```

## Parallelization

| Group | Tasks | Reason |
|-------|-------|--------|
| A | 1, 2, 3 | 顺序执行（删除→移动→更新引用） |

| Task | Depends On | Reason |
|------|------------|--------|
| 2 | 1 | 先删除快速上手指南，再移动其他规范文件 |
| 3 | 2 | 文件移动后才能更新引用路径 |
| 4 | 3 | 引用更新后再处理 dev_log |

---

## TODOs

- [ ] 1. 删除快速上手指南.md

  **What to do**:
  - 删除文件：`_docs/快速上手指南.md`
  - 搜索所有引用该文件的位置
  - 删除14个引用

  **Files to modify**:
  - `README.md`: 删除7个引用（快速开始部分的"详细指南"链接、文档索引、按角色查找文档、FAQ等）
  - `_docs/验证流程指南.md`: 删除1个引用（相关文档列表）
  - `_docs/business/README.md`: 删除1个引用（规范文档列表）
  - `_docs/specification/README.md`: 删除5个引用（目录说明、文档索引、使用指南）

  **Parallelizable**: NO (must be first)

  **References**:
  - 当前引用位置（来自 grep 结果）:
    - `README.md` 第60, 351, 491, 500, 505, 519, 553, 609行
    - `_docs/验证流程指南.md` 第403行
    - `_docs/business/README.md` 第95行
    - `_docs/specification/README.md` 第15, 28, 42, 68行

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] 文件已删除：`! [ -f _docs/快速上手指南.md ]` → command fails (file doesn't exist)
  - [ ] 引用已清除：`grep -r "快速上手指南" --include="*.md"` → 返回0结果
  - [ ] README.md 中快速开始部分只剩内联指南，不再有"详细指南"链接

  **Commit**: NO (group with 2, 3, 4)

---

- [ ] 2. 移动规范文件到 specification/

  **What to do**:
  - 移动4个规范文件从 `_docs/` 到 `_docs/specification/`
    - `业务代码编写规范.md`
    - `测试代码编写规范.md`
    - `测试示例指南.md`
    - `验证流程指南.md`
  - 验证移动成功

  **Parallelizable**: NO (depends on 1)

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] 文件已移动：`ls -la _docs/specification/ | grep "规范\|指南"` → 显示4个规范文件
  - [ ] 原位置无文件：`ls -la _docs/ | grep "规范\|指南"` → 返回0结果
  - [ ] specification 目录包含：README.md + 4个规范文件 = 5个文件

  **Commit**: NO (group with 1, 3, 4)

---

- [ ] 3. 更新规范文件引用路径

  **What to do**:
  - 更新所有对规范文件的引用，从 `_docs/文件名.md` 改为 `_docs/specification/文件名.md`
  - 影响文件（需要逐个检查和修改）:
    - `README.md` - 更新"文档导航"章节的所有链接
    - `_docs/business/README.md` - 更新规范文档列表的链接（已经指向 ../specification/，但需确认）
    - `_docs/specification/README.md` - 更新目录说明、文档索引的相对路径
    - `_docs/验证流程指南.md` - 如果有引用其他规范文件
    - `CLAUDE.md` - 更新文档导航部分

  **Path Mapping**:
  ```
  旧路径: _docs/业务代码编写规范.md
  新路径: _docs/specification/业务代码编写规范.md

  旧路径: _docs/测试代码编写规范.md
  新路径: _docs/specification/测试代码编写规范.md

  旧路径: _docs/测试示例指南.md
  新路径: _docs/specification/测试示例指南.md

  旧路径: _docs/验证流程指南.md
  新路径: _docs/specification/验证流程指南.md
  ```

  **Parallelizable**: NO (depends on 2)

  **References**:

  **当前引用检查**（执行任务时需要确认）:
  ```bash
  # 检查所有规范文件的引用
  grep -r "_docs/业务代码编写规范.md" --include="*.md"
  grep -r "_docs/测试代码编写规范.md" --include="*.md"
  grep -r "_docs/测试示例指南.md" --include="*.md"
  grep -r "_docs/验证流程指南.md" --include="*.md"
  ```

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] 旧路径引用已清除：
    - `grep -r "_docs/业务代码编写规范.md" --include="*.md"` → 0结果
    - `grep -r "_docs/测试代码编写规范.md" --include="*.md"` → 0结果
    - `grep -r "_docs/测试示例指南.md" --include="*.md"` → 0结果
    - `grep -r "_docs/验证流程指南.md" --include="*.md"` → 0结果
  - [ ] 新路径引用存在：
    - `grep -r "_docs/specification/业务代码编写规范.md" --include="*.md"` → N个结果
    - `grep -r "_docs/specification/测试代码编写规范.md" --include="*.md"` → N个结果
    - `grep -r "_docs/specification/测试示例指南.md" --include="*.md"` → N个结果
    - `grep -r "_docs/specification/验证流程指南.md" --include="*.md"` → N个结果

  **Commit**: NO (group with 1, 2, 4)

---

- [ ] 4. 删除 dev_log 目录及相关引用

  **What to do**:
  - 删除 `_docs/dev_log/` 目录及其下3个文件：
    - README.md
    - order_dev_log.md
    - search_dev_log.md
  - 从 `CLAUDE.md` 删除第6章"AI工作记录"整个章节（约100行）
  - 从 `_docs/specification/README.md` 删除 dev_log 条目（如果有）

  **Analysis**:
  - 根目录 `dev_log/` 已删除 ✅
  - `CLAUDE.md` 中有8处对 `dev_log/` 的引用（目录结构、命名规则、触发时机、标签体系、检索技巧、记录规范等）
  - 用户决策：删除整个 `_docs/dev_log/` 目录及其所有引用

  **Parallelizable**: NO (depends on 3)

  **References**:
  - `CLAUDE.md`: 第6章"AI工作记录"（约100行）
  - grep结果：8处 dev_log 引用

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `_docs/dev_log/` 目录已删除：`! [ -d _docs/dev_log ]` → command fails
  - [ ] CLAUDE.md 中无 dev_log 引用：`grep "dev_log" CLAUDE.md` → 0结果
  - [ ] specification/README.md 中无 dev_log 引用：`grep "dev_log" _docs/specification/README.md` → 0结果

  **Commit**: NO (group with 1, 2, 3)

---

- [ ] 5. 验证文档结构一致性

  **What to do**:
  - 检查所有文档中的目录结构描述是否与实际文件系统一致
  - 重点检查：
    - README.md 的"目录结构"章节
    - specification/README.md 的"目录说明"章节
    - CLAUDE.md 的文档导航部分

  **Parallelizable**: YES (with 4)

  **References**:
  - `README.md` 第147-207行（目录结构章节）
  - `_docs/specification/README.md` 第1-30行（目录说明）
  - `CLAUDE.md` 文档导航部分

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] README.md 的目录结构与实际一致：
    - `_docs/` 下应该没有规范文件（已移至 specification/）
    - 没有 `快速上手指南.md`
  - [ ] specification/README.md 的目录说明正确：
    - 列出4个规范文件
    - 不包含 `快速上手指南.md`
  - [ ] CLAUDE.md 的文档导航正确

  **Commit**: NO (group with 1, 2, 3, 4)

---

- [ ] 6. 最终验证和提交

  **What to do**:
  - 编译验证：`mvn clean compile`
  - 检查所有修改的文件
  - 提交所有更改

  **Parallelizable**: NO (depends on 1, 2, 3, 4, 5)

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] 编译成功：`mvn clean compile` → BUILD SUCCESS
  - [ ] 所有 markdown 文件语法正确：检查链接有效
  - [ ] 提交消息规范：`docs: restructure documentation and fix references`

  **Commit**: YES | Files: all modified markdown files

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 6 | `docs: restructure documentation and fix references` | README.md, _docs/specification/README.md, _docs/business/README.md, _docs/验证流程指南.md, CLAUDE.md | `mvn clean compile` |

---

## Success Criteria

### Verification Commands
```bash
# 验证快速上手指南删除
! [ -f _docs/快速上手指南.md ]

# 验证规范文件移动
ls _docs/specification/ | grep -E "(业务代码|测试代码|测试示例|验证流程)"

# 验证引用更新
grep -r "_docs/specification/业务代码编写规范.md" --include="*.md"

# 验证 dev_log 目录删除
! [ -d _docs/dev_log ]

# 验证 dev_log 引用清除
! grep "dev_log" CLAUDE.md

# 验证编译
mvn clean compile
```

### Final Checklist
- [ ] `快速上手指南.md` 已删除
- [ ] `_docs/dev_log/` 目录已删除
- [ ] 4个规范文件已在 `_docs/specification/` 目录
- [ ] 所有引用路径指向正确的规范文件位置
- [ ] dev_log 引用已从 CLAUDE.md 和 specification/README.md 清除
- [ ] 文档结构描述与实际文件系统一致
- [ ] 编译验证通过
