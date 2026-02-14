# Proposal: Add Checkstyle Configuration

## Why

项目代码注释和日志风格不一致，缺乏自动化检查手段。目前存在以下问题：

- 注释格式不统一（换行方式、@param 格式）
- 日志格式多样化（4 种不同格式）
- 异常日志丢失堆栈信息
- 缺乏代码风格强制约束

通过接入 Checkstyle，可以在编译期自动检查并阻断不符合规范的代码，确保团队代码风格一致性。

## What Changes

- **新增** Checkstyle Maven 插件配置
- **新增** `checkstyle.xml` 规则配置文件
- **新增** 自定义 Check 类（异常日志检查）
- **强制** 公开类/接口/枚举必须有 Javadoc
- **强制** public/protected 方法必须有 Javadoc（含 @param、@return）
- **强制** 日志使用占位符 `{}`，禁止字符串拼接
- **强制** 异常日志必须传递异常对象
- **强制** 统一导入顺序、行长度、缩进、命名规范

## Capabilities

### New Capabilities

- `checkstyle-config`: 代码风格检查配置，包含注释规范、日志规范、代码风格规范

### Modified Capabilities

无

## Impact

**影响范围**：

- 所有 Java 源文件（6 个模块：domain、app、infrastructure、adapter、start、test）
- Maven 构建流程（validate 阶段新增 Checkstyle 检查）
- CI/CD 流程（自动阻断不符合规范的代码）

**预期效果**：

- 构建时自动检查代码风格
- 违反规则时直接阻断构建（`failOnViolation: true`）
- 统一团队代码风格，提升代码可读性

**依赖变更**：

- 新增 `maven-checkstyle-plugin` 插件
- 新增 `checkstyle` 依赖（版本在根 POM 管理）
