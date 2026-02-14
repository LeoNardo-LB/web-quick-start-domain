# Tasks: Add Checkstyle Configuration

## 0. 前置准备

- [x] 0.1 阅读 AGENTS.md 了解技能加载要求
- [x] 0.2 使用 `/tdd-workflow` 命令加载 TDD 流程

## 1. 项目配置

- [x] 1.1 在根 POM 的 `<dependencyManagement>` 中添加 checkstyle 版本管理
- [x] 1.2 在根 POM 的 `<pluginManagement>` 中添加 maven-checkstyle-plugin 配置
- [x] 1.3 在根 POM 的 `<plugins>` 中激活 maven-checkstyle-plugin（validate 阶段）

## 2. Checkstyle 规则配置

- [x] 2.1 创建目录 `config/checkstyle/`
- [x] 2.2 创建 `config/checkstyle/checkstyle.xml` 主配置文件
- [x] 2.3 配置注释规范规则（JavadocType、JavadocMethod、JavadocStyle）
- [x] 2.4 配置 @param 格式检查规则（RegexpSinglelineJava）
- [x] 2.5 配置日志规范规则（禁止字符串拼接）
- [x] 2.6 配置代码风格规则（LineLength、Indentation、ImportOrder）
- [x] 2.7 配置命名规范规则（ConstantName、MemberName、MethodName、TypeName）

## 3. 自定义 Check 类（异常日志检查）

> **注**：使用 RegexpSinglelineJava 实现异常日志检查，无需创建自定义类

- [x] 3.1 ~~创建 `config/checkstyle/src/main/java/` 目录~~ （使用正则替代）
- [x] 3.2 ~~创建 `ExceptionLoggingCheck.java` 自定义检查类~~ （使用正则替代）
- [x] 3.3 实现 `log.error()` 必须传递异常对象的检查逻辑（RegexpSinglelineJava）
- [x] 3.4 ~~在 checkstyle.xml 中注册自定义 Check~~ （已使用 RegexpSinglelineJava）

## 4. Suppressions 配置

- [x] 4.1 创建 `config/checkstyle/checkstyle-suppressions.xml` 豁免文件
- [x] 4.2 在 checkstyle.xml 中引用 suppressions 文件
- [x] 4.3 豁免测试代码（MethodName、LineLength、Indentation、UnusedImports）

## 5. 验证与测试

- [x] 5.1 执行 `mvn clean compile` 验证配置正确加载
- [x] 5.2 确认违反规则时构建失败（`failOnViolation: true`）
- [x] 5.3 确认错误信息输出到控制台
- [x] 5.4 验证所有模块（domain、app、infrastructure、adapter、start、test）都纳入检查

## 6. 文档更新

- [x] 6.1 更新 README.md 添加 Checkstyle 使用说明
- [x] 6.2 在 AGENTS.md 中添加代码风格规范章节
