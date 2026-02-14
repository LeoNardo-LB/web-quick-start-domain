# Checkstyle Configuration Specification

## ADDED Requirements

### Requirement: 类必须有 Javadoc 注释

所有公开的类、接口、枚举必须有 Javadoc 注释，描述其用途。

**规则**: `JavadocType` + `MissingJavadocType`

- `scope: public`

#### Scenario: 公开类缺少 Javadoc

- **WHEN** 一个 public 类没有 Javadoc 注释
- **THEN** 构建失败，输出错误信息 `[JavadocType] 类缺少 Javadoc`

#### Scenario: 公开类有 Javadoc

- **WHEN** 一个 public 类有 Javadoc 注释
- **THEN** 构建通过

---

### Requirement: 方法必须有 Javadoc 注释

所有 public 和 protected 方法必须有 Javadoc 注释，包含 `@param` 和 `@return`（如有返回值）。

**规则**: `JavadocMethod` + `MissingJavadocMethod`

- `scope: protected`
- `allowMissingParamTags: false`
- `allowMissingReturnTag: false`

#### Scenario: public 方法缺少 Javadoc

- **WHEN** 一个 public 方法没有 Javadoc 注释
- **THEN** 构建失败，输出错误信息

#### Scenario: protected 方法缺少 Javadoc

- **WHEN** 一个 protected 方法没有 Javadoc 注释
- **THEN** 构建失败，输出错误信息

#### Scenario: 方法缺少 @param

- **WHEN** 一个方法有参数但没有 @param 注解
- **THEN** 构建失败，输出错误信息

#### Scenario: 方法缺少 @return

- **WHEN** 一个方法有返回值但没有 @return 注解
- **THEN** 构建失败，输出错误信息

---

### Requirement: @param 格式规范

`@param` 注解必须使用标准格式：`@param paramName 参数描述`（参数名后有空格）。

**规则**: `RegexpSinglelineJava`（自定义正则）

#### Scenario: @param 缺少空格

- **WHEN** Javadoc 中存在 `@param paramName描述`（无空格）
- **THEN** 构建失败，输出错误信息 `@param 格式错误：参数名后需要空格描述`

#### Scenario: @param 格式正确

- **WHEN** Javadoc 中存在 `@param paramName 参数描述`（有空格）
- **THEN** 构建通过

---

### Requirement: 日志禁止字符串拼接

所有日志调用必须使用占位符 `{}`，禁止使用 `+` 拼接字符串。

**规则**: `RegexpSinglelineJava`

- 正则: `log\.(info|debug|warn|error)\([^)]*\+[^)]*\)`

#### Scenario: 日志使用字符串拼接

- **WHEN** 代码中存在 `log.info("订单: " + orderId)`
- **THEN** 构建失败，输出错误信息 `日志禁止使用字符串拼接，请使用占位符 {}`

#### Scenario: 日志使用占位符

- **WHEN** 代码中存在 `log.info("订单: {}", orderId)`
- **THEN** 构建通过

---

### Requirement: 异常日志必须传递异常对象

`log.error()` 调用时，最后一个参数必须是异常对象（Throwable），确保堆栈信息不丢失。

**规则**: 自定义 Check 类 `ExceptionLoggingCheck`

#### Scenario: 异常日志仅传递消息

- **WHEN** 代码中存在 `log.error("异常: {}", e.getMessage())`
- **THEN** 构建失败，输出错误信息 `异常日志必须传递异常对象`

#### Scenario: 异常日志传递异常对象

- **WHEN** 代码中存在 `log.error("异常", e)`
- **THEN** 构建通过

#### Scenario: 异常日志传递异常对象和参数

- **WHEN** 代码中存在 `log.error("异常: orderId={}", orderId, e)`
- **THEN** 构建通过

---

### Requirement: 行长度限制

每行代码长度不超过 120 字符。

**规则**: `LineLength`

- `max: 120`

#### Scenario: 行长度超过限制

- **WHEN** 一行代码超过 120 字符
- **THEN** 构建失败，输出错误信息

#### Scenario: 行长度在限制内

- **WHEN** 一行代码不超过 120 字符
- **THEN** 构建通过

---

### Requirement: 缩进规范

代码缩进使用 4 空格。

**规则**: `Indentation`

- `basic: 4`
- `caseIndent: 4`

#### Scenario: 缩进不正确

- **WHEN** 代码使用非 4 空格缩进
- **THEN** 构建失败，输出错误信息

#### Scenario: 缩进正确

- **WHEN** 代码使用 4 空格缩进
- **THEN** 构建通过

---

### Requirement: 导入顺序规范

导入语句按以下顺序分组：

1. `java.*`
2. `jakarta.*`
3. 第三方库（如 `org.springframework.*`）
4. 项目内部包（`org.smm.archetype.*`）

每组之间空一行。

**规则**: `ImportOrder`

- `groups: java, jakarta, thirdparty, project`
- `separated: true`
- `option: top`

#### Scenario: 导入顺序不正确

- **WHEN** 导入语句未按分组顺序排列
- **THEN** 构建失败，输出错误信息

#### Scenario: 导入顺序正确

- **WHEN** 导入语句按分组顺序排列且组间有空行
- **THEN** 构建通过

---

### Requirement: 命名规范

| 类型      | 规则                              | 示例            |
|---------|---------------------------------|---------------|
| 常量      | `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$` | `MAX_COUNT`   |
| 局部变量    | `^[a-z][a-zA-Z0-9]*$`           | `orderCount`  |
| 成员变量    | `^[a-z][a-zA-Z0-9]*$`           | `customerId`  |
| 方法      | `^[a-z][a-zA-Z0-9]*$`           | `createOrder` |
| 参数      | `^[a-z][a-zA-Z0-9]*$`           | `orderId`     |
| 类/接口/枚举 | `^[A-Z][a-zA-Z0-9]*$`           | `OrderAggr`   |

**规则**: `ConstantName`, `LocalVariableName`, `MemberName`, `MethodName`, `ParameterName`, `TypeName`

#### Scenario: 常量命名不符合规范

- **WHEN** 常量命名为 `maxCount` 而非 `MAX_COUNT`
- **THEN** 构建失败，输出错误信息

#### Scenario: 方法命名不符合规范

- **WHEN** 方法命名为 `CreateOrder` 而非 `createOrder`
- **THEN** 构建失败，输出错误信息

#### Scenario: 命名符合规范

- **WHEN** 所有命名符合规范
- **THEN** 构建通过

---

### Requirement: 构建阻断

当任何 Checkstyle 规则违反时，Maven 构建必须失败。

**配置**: `failOnViolation: true`

#### Scenario: 存在规则违反

- **WHEN** 代码违反任何 Checkstyle 规则
- **THEN** Maven 构建失败，返回非零退出码

#### Scenario: 无规则违反

- **WHEN** 代码通过所有 Checkstyle 规则
- **THEN** Maven 构建成功
