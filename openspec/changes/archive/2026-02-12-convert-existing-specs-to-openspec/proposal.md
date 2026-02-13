# 转换现有规范为OpenSpec格式

## Why

项目当前在`specs/`目录下有4个功能规范文档，使用的是spec-kit的旧格式：
- `001-test-standardization` - 测试用例标准化与规范
- `1-fix-logging-output` - 修复日志输出路径并优化日志配置
- `002-constitution-compliance` - 宪章合规性代码审查与修复
- `001-guava-desensitize` - Guava脱敏优化

这些规范存在以下问题：
1. **格式不统一**：与OpenSpec的spec格式不一致，缺少统一的scenario格式
2. **难以管理**：没有统一的变更管理和版本控制机制
3. **缺乏规范性**：没有强制使用SHALL/MUST关键词，规范约束力不足

通过转换为OpenSpec格式，可以：
- 统一规范管理，使用OpenSpec的spec-driven工作流
- 提供更强的规范性约束（SHALL/MUST关键词）
- 支持规范的增量更新和版本管理

## What Changes

### 规范转换
- 将4个现有规范文档转换为OpenSpec spec格式
- 保持原有内容，调整格式以符合OpenSpec要求
- 确保所有scenario使用4个hashtags（####）

### 格式标准化
- 所有Requirement描述添加SHALL或MUST关键词
- 统一scenario格式：WHEN/THEN结构
- 保持原有业务逻辑和验收标准不变

## Capabilities

### New Capabilities

#### test-standardization
测试用例标准化与规范，包括：
- 测试目录结构标准化（org.smm.archetype.test.cases）
- 测试类命名规范化（ITest/UTest）
- 测试基类继承规范化（IntegrationTestBase/UnitTestBase）
- 测试类型判断规范（大入口类vs简单类）

#### fix-logging-output
修复日志输出路径并优化日志配置，包括：
- 日志输出路径修复（输出到项目内部.logs目录）
- 日志配置优化（异步队列、格式统一）
- 环境差异化配置（dev/production）
- 敏感信息脱敏和审计日志

#### constitution-compliance
宪章合规性代码审查与修复，包括：
- 高内聚原则违规修复（内部类可见性）
- 值对象违规修复（不可变性、equalityFields）
- 仓储违规修复（位置、命名、实现）
- Response DTO违规修复（@Builder模式）

#### guava-desensitize
Guava脱敏优化，包括：
- 重新启用脱敏功能
- 使用Guava优化脱敏实现
- 7种敏感信息类型脱敏（密码、Token、手机号、身份证号、银行卡号、IP地址、邮箱）

## Impact

### 影响范围
- **规范文档**：4个规范文档格式转换
- **开发流程**：无影响，仅规范格式调整
- **代码质量**：通过规范约束提升代码质量

### 风险与缓解
- **风险**：转换过程中可能丢失部分细节
  - **缓解**：保持原有内容，仅调整格式
- **风险**：格式转换可能导致理解偏差
  - **缓解**：保留原文档作为参考，转换后进行验证

### 预期收益
- **规范统一**：所有规范使用统一的OpenSpec格式
- **管理便捷**：使用OpenSpec工作流管理规范变更
- **约束增强**：SHALL/MUST关键词提供更强的规范约束
