# 文档去冗余重构

## 概述

精简项目文档，消除冗余内容，建立统一的引用机制，避免多处维护同一内容。

## 问题分析

### 发现的冗余

| 文档 A | 文档 B | 冗余内容 |
|--------|--------|---------|
| `verification-workflow.md` | `constitution.md §XXXII` | TDD 验证流程 |
| `verification-workflow.md` | `tdd-workflow` skill | 验证流程、覆盖率要求 |
| `test/AGENTS.md` | `constitution.md §XVIII-XIX` | 单元测试/集成测试规范 |

### 解决方案

采用 **单一职责 + 引用机制**：

```
文档层次：
├── constitution.md          # 规则（强制约束，非详细流程）
│   └── §XXXII 引用 → tdd-workflow skill
│
├── .opencode/skills/tdd-workflow/  # TDD 流程主文档
│   ├── SKILL.md             # 五阶段流程概要
│   ├── scripts/             # 测试脚本
│   └── references/          # 各语言详细适配器
│
├── AGENTS.md               # 项目概览（引用 skill）
├── test/AGENTS.md          # 测试模块结构（引用宪章）
└── README.md               # 项目说明（引用 skill）
```

## 变更内容

### 新增

| 文件 | 说明 |
|------|------|
| `.opencode/skills/tdd-workflow/` | TDD Workflow Skill（多语言泛化版） |
| `scripts/python/lsp_client.py` | LSP 客户端封装 |
| `scripts/python/run-unit-tests.py` | 单元测试脚本（Git + LSP） |
| `scripts/python/run-integration-tests.py` | 集成测试脚本（Git + LSP） |
| `scripts/python/run-sample-tests.py` | 抽检脚本 |
| `references/java-spring.md` | Java/Spring Boot 适配器 |
| `references/python-pytest.md` | Python/pytest 适配器 |
| `references/js-jest.md` | JavaScript/Jest 适配器 |
| `references/go-testing.md` | Go/testing 适配器 |

### 删除

| 文件 | 原因 |
|------|------|
| `.specify/memory/verification-workflow.md` | 内容迁移到 tdd-workflow skill |
| `scripts/run-unit-tests.sh` | 替换为 Python 跨平台版本 |
| `scripts/run-integration-tests.sh` | 替换为 Python 跨平台版本 |
| `scripts/run-sample-tests.sh` | 替换为 Python 跨平台版本 |

### 修改

| 文件 | 变更 |
|------|------|
| `constitution.md §XXXII` | 精简为 ~15 行，引用 tdd-workflow skill |
| `test/AGENTS.md` | 精简测试规范细节，引用宪章 |
| `AGENTS.md` | 更新引用，指向 tdd-workflow skill |
| `README.md` | 更新 4 处引用，指向 tdd-workflow skill |

### 清理

| 文件/目录 | 原因 |
|----------|------|
| `D:Developcodeminearchetypeweb-quick-start-domainscriptspython` | Windows 路径错误产生的垃圾 |
| `D:Developcodeminearchetypeweb-quick-start-domainscripts.test-cache` | Windows 路径错误产生的垃圾 |
| `nul` | Windows 特殊设备名误创建 |
| `test-output.txt` (69MB) | 测试输出文件 |

## 文档规范

### 新增规范

1. **Skill 优先**：详细流程应放入 skill，规范文档只保留规则和引用
2. **单一职责**：每个文档只负责一类信息
3. **引用优于复制**：使用 Markdown 链接引用其他文档

### 引用模式

```markdown
> **📌 强制加载 Skill**：编码时必须加载 `tdd-workflow` skill。
>
> 位置：`.opencode/skills/tdd-workflow/`
```

## 影响

- 减少文档维护成本
- 避免多处修改导致不一致
- 统一 TDD 流程入口
- 支持多语言泛化
