# Design: Checkstyle Configuration

## Context

**背景**：项目采用 DDD 四层架构，包含 6 个 Maven 模块，约 100+ Java 文件。当前代码风格不一致，主要体现在：

- 注释格式多样化（有无换行、@param 格式）
- 日志格式不统一（4 种不同格式）
- 异常日志部分丢失堆栈信息
- 缺乏自动化检查手段

**约束**：

- 必须与现有 Maven 构建流程集成
- 构建失败必须阻断（failOnViolation: true）
- 不影响现有 TDD 验证流程

## Goals / Non-Goals

**Goals:**

- ✅ 统一注释规范（类/方法必须有 Javadoc）
- ✅ 统一日志规范（占位符、异常堆栈）
- ✅ 统一代码风格（导入顺序、行长度、缩进、命名）
- ✅ 编译期自动检查，违反规则直接阻断构建

**Non-Goals:**

- ❌ 不修改现有代码风格（本次仅新增检查规则）
- ❌ 不集成到 TDD 验证流程（独立运行）
- ❌ 不配置 IDE 自动格式化规则

## Decisions

### Decision 1: Checkstyle 版本选择

**选择**: `checkstyle 10.17.0`（最新稳定版）

**原因**:

- 支持 Java 25 语法
- 支持自定义 Check 类
- 社区活跃，文档完善

**替代方案**:

- SpotBugs：主要用于缺陷检测，不适合风格检查
- PMD：规则较少，自定义能力弱

### Decision 2: 规则配置文件位置

**选择**: `config/checkstyle/checkstyle.xml`

**原因**:

- 符合 Maven 约定
- 与 `checkstyle-suppressions.xml` 同级，便于管理
- 独立于模块，全局生效

### Decision 3: 注释规范规则

**规则配置**:

| 规则                   | 配置                          | 说明                             |
|----------------------|-----------------------------|--------------------------------|
| JavadocType          | `scope: public`             | 公开类/接口/枚举必须有 Javadoc           |
| JavadocMethod        | `scope: protected`          | public/protected 方法必须有 Javadoc |
| JavadocStyle         | `checkFirstSentence: false` | 允许无首句描述                        |
| MissingJavadocType   | `scope: public`             | 检查缺失的类 Javadoc                 |
| MissingJavadocMethod | `scope: protected`          | 检查缺失的方法 Javadoc                |

**@param 格式检查**:

```xml
<!-- 使用正则检查 @param 格式：参数名后必须有空格 -->
<module name="RegexpSinglelineJava">
    <property name="format" value="@param\s+\w+\s+[^@\s]"/>
    <property name="message" value="@param 格式错误：参数名后需要空格描述"/>
</module>
```

### Decision 4: 日志规范规则

**禁止字符串拼接**:

```xml

<module name="RegexpSinglelineJava">
    <property name="format" value="log\.(info|debug|warn|error)\([^)]*\+[^)]*\)"/>
    <property name="message" value="日志禁止使用字符串拼接，请使用占位符 {}"/>
</module>
```

**异常日志检查**（自定义 Check）:

```java
// 检查 log.error 是否传递了异常对象
// 禁止：log.error("消息: {}", e.getMessage())
// 允许：log.error("消息", e)
```

### Decision 5: 代码风格规则

| 规则                | 配置                              | 说明            |
|-------------------|---------------------------------|---------------|
| LineLength        | `max: 120`                      | 行长度不超过 120 字符 |
| Indentation       | `basic: 4, caseIndent: 4`       | 4 空格缩进        |
| ImportOrder       | 按包名分组排序                         | 统一导入顺序        |
| ConstantName      | `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$` | 常量大写下划线       |
| LocalVariableName | `^[a-z][a-zA-Z0-9]*$`           | 局部变量驼峰        |
| MemberName        | `^[a-z][a-zA-Z0-9]*$`           | 成员变量驼峰        |
| MethodName        | `^[a-z][a-zA-Z0-9]*$`           | 方法名驼峰         |
| ParameterName     | `^[a-z][a-zA-Z0-9]*$`           | 参数名驼峰         |
| TypeName          | `^[A-Z][a-zA-Z0-9]*$`           | 类名帕斯卡         |

### Decision 6: Maven 插件配置

**插件版本**: `maven-checkstyle-plugin 3.5.0`

**执行阶段**: `validate`（编译前检查）

**配置要点**:

- `failOnViolation: true`（违反规则阻断构建）
- `consoleOutput: true`（输出到控制台）
- `includeTestSourceDirectory: true`（包含测试代码）

## Risks / Trade-offs

| 风险             | 缓解措施                             |
|----------------|----------------------------------|
| 现有代码可能无法通过检查   | 使用 `suppressions.xml` 临时豁免历史代码   |
| 团队适应成本         | 提供 IDE 模板和文档                     |
| 构建时间增加         | Checkstyle 在 validate 阶段，不影响编译速度 |
| 自定义 Check 维护成本 | 代码量小（约 50 行），注释完整                |

## Open Questions

无
