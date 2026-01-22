# Combined Documentation Work Plan

## Context

### Original Request
用户要求依次执行三个文档优化计划：
1. **Plan 3**: 清理 dev_log 失效引用
2. **Plan 1**: 文档清理和重构（删除快速上手指南、移动规范文件、清理dev_log）
3. **Plan 2**: Markdown文档引用全面优化

### Execution Order
**用户指定顺序**: Plan 3 → Plan 1 → Plan 2

### Plan Dependencies
- Plan 3 独立执行（清理 dev_log 引用）
- Plan 1 包含 dev_log 清理，但 Plan 3 会先完成这部分
- Plan 2 修复 README.md 中的路径，会与 Plan 1 的移动规范文件操作有重叠

---

## Work Objectives

### Core Objective
按照指定顺序依次执行三个文档优化计划，确保文档结构正确、引用有效。

### Concrete Deliverables
- 清理所有 dev_log 引用（Plan 3）
- 删除重复的快速上手指南.md（Plan 1）
- 移动规范文件到 _docs/specification/（Plan 1）
- 更新所有文档引用路径（Plan 1 + Plan 2）
- 删除死链引用（Plan 2）

### Definition of Done
- [ ] 所有 dev_log 引用已清理
- [ ] 快速上手指南.md 已删除
- [ ] 规范文件已在 _docs/specification/ 目录
- [ ] 所有文档引用路径正确
- [ ] 死链引用已删除
- [ ] 文档结构描述与实际一致
- [ ] 编译验证通过

### Must Have
- 按照用户指定顺序执行（3 → 1 → 2）
- 确保所有修改的文档格式正确
- 保持文档的完整性和可读性

### Must NOT Have (Guardrails)
- 不得遗漏任何引用修复
- 不得破坏文档结构
- 不得保留死链引用

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: NO
- **Framework**: none

### Manual QA Only

所有验证为手动命令行验证：

| 验证类型 | 命令 | 预期结果 |
|---------|-------|---------|
| 文件删除 | `! [ -f _docs/快速上手指南.md ]` | command fails |
| 文件移动 | `ls _docs/specification/ \| grep "规范\|指南"` | 显示4个规范文件 |
| dev_log 清理 | `grep -r "dev_log" --include="*.md" .` | 无匹配 |
| 编译 | `mvn clean compile` | BUILD SUCCESS |

---

## Task Flow

```
Plan 3 (clean-dev-log)
  ├─ Task 3.1 → Task 3.2 → Task 3.3

Plan 1 (documentation-cleanup)
  ├─ Task 1.1 → Task 1.2 → Task 1.3 → Task 1.4 → Task 1.5 → Task 1.6

Plan 2 (fix-markdown-references)
  ├─ Task 2.1 → Task 2.2 → Task 2.3 → Task 2.4 → Task 2.5 → Task 2.6
```

---

## Parallelization

无并行任务，所有任务顺序执行。

---

## TODOs

### === PLAN 3: 清理 dev_log 失效引用 ===

- [ ] 3.1. 修改 archetype_quickstart.md - 删除 dev_log 配置引用

  **What to do**:
  - 删除第196行 `excludePatterns` 配置中的 `dev_log/**` 模式
  - 删除第208行关于 `dev_log/**` 的说明项
  - 保持配置格式的一致性

  **Must NOT do**:
  - 不得删除其他有效的排除模式项
  - 不得修改其他配置说明

  **Parallelizable**: NO

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `grep "dev_log" archetype_quickstart.md` → 无匹配结果
  - [ ] 查看 archetype_quickstart.md 第196-208行，确认删除正确

  **Commit**: NO

---

- [ ] 3.2. 修改 _docs/specification/README.md - 删除文档导航链接

  **What to do**:
  - 删除第120行的整个 [AI工作记录] 链接项
  - 保持其他链接项的格式

  **Must NOT do**:
  - 不得删除其他有效的文档导航链接
  - 不得破坏文档的结构

  **Parallelizable**: NO

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `grep "dev_log" _docs/specification/README.md` → 无匹配结果
  - [ ] 查看 _docs/specification/README.md 第116-122行，确认删除正确

  **Commit**: NO

---

- [ ] 3.3. 验证 Plan 3 所有修改完成

  **What to do**:
  - 全局搜索确认所有 dev_log 引用已清理
  - 检查修改后的文档格式正确性

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `grep -r "dev_log" --include="*.md" .` → 无匹配结果
  - [ ] archetype_quickstart.md 和 _docs/specification/README.md 格式正确

  **Commit**: YES
  - Message: `docs: remove dead references to deleted dev_log directory`
  - Files: `archetype_quickstart.md`, `_docs/specification/README.md`

---

### === PLAN 1: 文档清理和重构 ===

- [ ] 1.1. 删除快速上手指南.md

  **What to do**:
  - 删除文件：`_docs/快速上手指南.md`
  - 从4个文件中删除14个引用

  **Files to modify**:
  - `README.md`: 删除7个引用
  - `_docs/验证流程指南.md`: 删除1个引用
  - `_docs/business/README.md`: 删除1个引用
  - `_docs/specification/README.md`: 删除5个引用

  **Parallelizable**: NO

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `! [ -f _docs/快速上手指南.md ]` → command fails
  - [ ] `grep -r "快速上手指南" --include="*.md"` → 无匹配结果

  **Commit**: NO

---

- [ ] 1.2. 移动规范文件到 specification/

  **What to do**:
  - 移动4个规范文件从 `_docs/` 到 `_docs/specification/`
    - `业务代码编写规范.md`
    - `测试代码编写规范.md`
    - `测试示例指南.md`
    - `验证流程指南.md`

  **Parallelizable**: NO

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `ls -la _docs/specification/ | grep "规范\|指南"` → 显示4个规范文件
  - [ ] `ls -la _docs/ | grep "规范\|指南"` → 无匹配结果

  **Commit**: NO

---

- [ ] 1.3. 更新规范文件引用路径

  **What to do**:
  - 更新所有对规范文件的引用，从 `_docs/文件名.md` 改为 `_docs/specification/文件名.md`
  - 影响文件：README.md, _docs/business/README.md, _docs/specification/README.md, _docs/验证流程指南.md, CLAUDE.md

  **Path Mapping**:
  ```
  _docs/业务代码编写规范.md → _docs/specification/业务代码编写规范.md
  _docs/测试代码编写规范.md → _docs/specification/测试代码编写规范.md
  _docs/测试示例指南.md → _docs/specification/测试示例指南.md
  _docs/验证流程指南.md → _docs/specification/验证流程指南.md
  ```

  **Parallelizable**: NO

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `grep -r "_docs/业务代码编写规范.md" --include="*.md"` → 无匹配
  - [ ] `grep -r "_docs/测试代码编写规范.md" --include="*.md"` → 无匹配
  - [ ] `grep -r "_docs/测试示例指南.md" --include="*.md"` → 无匹配
  - [ ] `grep -r "_docs/验证流程指南.md" --include="*.md"` → 无匹配
  - [ ] `grep -r "_docs/specification/业务代码编写规范.md" --include="*.md"` → N个结果

  **Commit**: NO

---

- [ ] 1.4. 删除 dev_log 目录及相关引用

  **What to do**:
  - 删除 `_docs/dev_log/` 目录及其下3个文件
  - 从 `CLAUDE.md` 删除第6章"AI工作记录"整个章节
  - 从 `_docs/specification/README.md` 删除 dev_log 条目

  **Note**: Plan 3 已清理大部分 dev_log 引用，此任务主要处理：
  - 删除 _docs/dev_log/ 目录
  - 从 CLAUDE.md 删除第6章（约100行）

  **Parallelizable**: NO

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `! [ -d _docs/dev_log ]` → command fails
  - [ ] `grep "dev_log" CLAUDE.md` → 无匹配结果
  - [ ] `grep "dev_log" _docs/specification/README.md` → 无匹配结果

  **Commit**: NO

---

- [ ] 1.5. 验证文档结构一致性

  **What to do**:
  - 检查所有文档中的目录结构描述是否与实际文件系统一致
  - 重点检查：README.md, specification/README.md, CLAUDE.md

  **Parallelizable**: NO

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] README.md 的目录结构与实际一致
  - [ ] specification/README.md 的目录说明正确
  - [ ] CLAUDE.md 的文档导航正确

  **Commit**: NO

---

- [ ] 1.6. 最终验证和提交

  **What to do**:
  - 编译验证：`mvn clean compile`
  - 检查所有修改的文件
  - 提交所有更改

  **Parallelizable**: NO

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] `mvn clean compile` → BUILD SUCCESS
  - [ ] 所有 markdown 文件语法正确

  **Commit**: YES
  - Message: `docs: restructure documentation and fix references`
  - Files: README.md, _docs/specification/README.md, _docs/business/README.md, _docs/验证流程指南.md, CLAUDE.md

---

### === PLAN 2: Markdown文档引用全面优化 ===

- [x] 2.1. 修正 README.md 中的错误路径引用（已在之前完成）

  **Status**: ✅ COMPLETED
  - 修正了6处路径错误（改为 _docs/specification/）

  **Commit**: NO

---

- [x] 2.2. 删除 README.md 中对模块 README 的死链引用（已在之前完成）

  **Status**: ✅ COMPLETED
  - 删除了所有 domain/README.md, app/README.md 等引用

  **Commit**: NO

---

- [x] 2.3. 删除 README.md 中对 CLAUDE.md 的引用（已在之前完成）

  **Status**: ✅ COMPLETED
  - 删除了 CLAUDE.md 引用

  **Commit**: NO

---

- [x] 2.4. 调整 README.md 中的其他相关内容（已在之前完成）

  **Status**: ✅ COMPLETED
  - 调整了文档上下文内容

  **Commit**: NO

---

- [x] 2.5. 删除 _docs/business/README.md 中的死链引用（已在之前完成）

  **Status**: ✅ COMPLETED
  - 删除了 CLAUDE.md 和模块 README 引用

  **Commit**: NO

---

- [x] 2.6. 全面验证所有修改（已在之前完成）

  **Status**: ✅ COMPLETED
  - 全局验证所有死链已清理

  **Commit**: YES
  - Message: `docs: fix all dead links and incorrect paths in markdown documentation`
  - Files: README.md, _docs/business/README.md

---

## Commit Strategy

| Step | Message | Files | Verification |
|------|---------|-------|--------------|
| Plan 3 完成 | `docs: remove dead references to deleted dev_log directory` | archetype_quickstart.md, _docs/specification/README.md | grep -r "dev_log" |
| Plan 1 完成 | `docs: restructure documentation and fix references` | README.md, _docs/specification/README.md, _docs/business/README.md, _docs/验证流程指南.md, CLAUDE.md | mvn clean compile |
| Plan 2 | `docs: fix all dead links and incorrect paths` | README.md, _docs/business/README.md | 已完成 |

---

## Success Criteria

### Verification Commands
```bash
# 验证快速上手指南删除
! [ -f _docs/快速上手指南.md ]

# 验证规范文件移动
ls _docs/specification/ | grep -E "(业务代码|测试代码|测试示例|验证流程)"

# 验证 dev_log 清理
! [d _docs/dev_log ]
! grep "dev_log" CLAUDE.md

# 验证编译
mvn clean compile
```

### Final Checklist
- [ ] Plan 3: dev_log 引用已清理
- [ ] Plan 1: 快速上手指南.md 已删除
- [ ] Plan 1: 4个规范文件已在 _docs/specification/
- [ ] Plan 1: 所有引用路径已更新
- [ ] Plan 1: dev_log 目录已删除
- [ ] Plan 1: 文档结构描述一致
- [ ] Plan 2: 所有死链引用已删除
- [ ] Plan 2: 所有路径引用正确
- [ ] 编译验证通过
