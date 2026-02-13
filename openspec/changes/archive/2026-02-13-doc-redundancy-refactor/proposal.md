# Proposal: 文档去冗余重构

## 问题

项目文档存在多处冗余：
1. TDD 验证流程在 `verification-workflow.md` 和 `constitution.md` 中重复
2. 测试规范在 `test/AGENTS.md` 和 `constitution.md` 中重复
3. 脚本命令在多处文档中重复

## 方案

建立 **Skill + 规范** 的分层架构：
- **Skill**：存放详细流程、脚本、适配器
- **规范**：只保留规则，引用 Skill

## 影响

- 减少维护成本
- 避免不一致
- 支持多语言

## 状态

已完成实施。
