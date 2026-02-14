## ADDED Requirements

### Requirement: 测试环境配置规范

测试代码必须使用独立的配置文件，与生产环境配置解耦。

#### Scenario: 测试配置文件位置

- **WHEN** 创建测试环境配置
- **THEN** `application.yaml` 必须位于 `test/src/test/resources/`
- **THEN** `logback-test.xml` 必须位于 `test/src/test/resources/`
- **THEN** 测试配置文件不应引用生产配置文件

#### Scenario: 测试数据库配置

- **WHEN** 运行需要数据库的测试
- **THEN** 必须使用 H2 内存数据库
- **THEN** H2 必须配置为 MySQL 兼容模式
- **THEN** 禁止测试代码连接外部数据库

---

### Requirement: 技术债务管理

测试代码中禁止保留过时的调试代码和误导性注释。

#### Scenario: @Disabled 注解清理

- **WHEN** 测试问题已修复
- **THEN** 必须移除相关的 `@Disabled` 注解
- **THEN** 必须验证测试能正常通过

#### Scenario: 过时注释清理

- **WHEN** 注释描述的问题已解决
- **THEN** 必须更新或移除该注释
- **THEN** 禁止保留描述"已修复问题"的注释

#### Scenario: 临时代码标记

- **WHEN** 添加临时的调试或修复代码
- **THEN** 必须添加 `// TODO:` 或 `// FIXME:` 标记
- **THEN** 必须创建对应的 Issue 跟踪清理工作
