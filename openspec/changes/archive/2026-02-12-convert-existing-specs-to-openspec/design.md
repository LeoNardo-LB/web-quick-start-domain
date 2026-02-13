# 转换现有规范为OpenSpec格式 - 技术设计

## Context

**当前状态**：

项目在`specs/`目录下有4个功能规范文档，使用spec-kit的旧格式：
- `001-test-standardization/spec.md` - 281行
- `1-fix-logging-output/spec.md` - 209行
- `002-constitution-compliance/spec.md` - 223行
- `001-guava-desensitize/spec.md` - 231行

OpenSpec格式要求：
- 使用`## ADDED Requirements`或`## MODIFIED Requirements`标题
- 使用`### Requirement:`格式
- Requirement描述必须包含SHALL或MUST关键词
- 使用`#### Scenario:`格式（4个hashtags）
- Scenario使用WHEN/THEN结构

**约束条件**：
- 必须保持原有业务逻辑和验收标准不变
- 不能丢失任何现有的规范细节
- 转换后的规范必须通过OpenSpec验证

## Goals / Non-Goals

**Goals:**

1. **格式转换**
   - 将4个现有规范文档转换为OpenSpec spec格式
   - 确保所有格式符合OpenSpec要求

2. **保持完整性**
   - 保持原有业务逻辑和验收标准不变
   - 不丢失任何现有的规范细节

3. **增强规范性**
   - 为所有Requirement添加SHALL或MUST关键词
   - 统一scenario格式

**Non-Goals:**

- 不修改规范的业务逻辑或技术方案
- 不添加新的规范内容
- 不删除现有的规范内容

## Decisions

### 决策1：逐个转换规范，每个作为独立spec

**决策**：将4个现有规范分别转换为4个独立的OpenSpec spec文档

**理由**：
- 每个规范关注的领域不同（测试、日志、合规、脱敏）
- 独立spec便于独立更新和版本管理
- 符合OpenSpec的单一职责原则

**替代方案考虑**：
- [方案A] 合并为一个spec
  - 反对：违反单一职责原则，难以维护
  - 反对：不同规范可能需要独立更新

### 决策2：保持原有User Story转换为Requirement

**决策**：将原有的User Story直接转换为Requirement，保持原有结构

**理由**：
- User Story已经包含了完整的业务场景
- 转换为Requirement格式更符合OpenSpec规范
- 保持原有的优先级和验收场景

**替代方案考虑**：
- [方案A] 完全重新组织结构
  - 反对：可能丢失原有逻辑
  - 反对：增加转换工作量

### 决策3：为Requirement描述添加SHALL/MUST关键词

**决策**：根据规范性质选择SHALL或MUST关键词

**理由**：
- SHALL用于接口规范和行为约束
- MUST用于强制要求和非协商性规则
- 符合RFC 2119规范

**关键词选择标准**：
- **SHALL**：用于功能性要求、接口规范、行为描述
- **MUST**：用于强制性要求、非协商性规则、质量标准

## Risks / Trade-offs

### 风险1：转换过程中可能丢失细节

**风险描述**：格式转换过程中可能遗漏部分规范细节

**缓解措施**：
- 转换完成后逐条对比原文档
- 保留原文档作为参考
- 进行OpenSpec验证确保完整性

### 风险2：SHALL/MUST关键词选择不当

**风险描述**：关键词选择可能导致规范约束力过强或过弱

**缓解措施**：
- 根据RFC 2119标准选择关键词
- 对非协商性规则使用MUST
- 对功能性要求使用SHALL

### 权衡：转换效率 vs 完整性

**权衡分析**：
- 快速转换可能遗漏细节
- 仔细转换需要更多时间
- 选择仔细转换，确保完整性

**决策**：优先保证完整性，转换完成后进行验证

## Migration Plan

### 第一步：转换test-standardization规范

1. 读取原文档
2. 将User Story转换为Requirement
3. 添加SHALL/MUST关键词
4. 验证格式符合OpenSpec要求

### 第二步：转换fix-logging-output规范

1. 读取原文档
2. 将User Story转换为Requirement
3. 添加SHALL/MUST关键词
4. 验证格式符合OpenSpec要求

### 第三步：转换constitution-compliance规范

1. 读取原文档
2. 将User Story转换为Requirement
3. 添加SHALL/MUST关键词
4. 验证格式符合OpenSpec要求

### 第四步：转换guava-desensitize规范

1. 读取原文档
2. 将User Story转换为Requirement
3. 添加SHALL/MUST关键词
4. 验证格式符合OpenSpec要求

### 第五步：验证和归档

1. 运行OpenSpec验证
2. 确认所有spec格式正确
3. 归档变更

## Open Questions

1. **是否需要保留原文档**：转换完成后是否保留`specs/`目录下的原文档？
2. **版本兼容性**：转换后的规范如何与现有代码保持兼容？
