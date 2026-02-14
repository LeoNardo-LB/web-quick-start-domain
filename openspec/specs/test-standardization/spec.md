# test-standardization Specification

## Purpose
TBD - created by archiving change convert-existing-specs-to-openspec. Update Purpose after archive.
## Requirements
### Requirement: 测试目录结构标准化

The system SHALL organize all test files under the `org.smm.archetype.test.cases` directory, separating integration tests and unit tests into different subdirectories.

#### Scenario: 集成测试包路径

- **WHEN** a developer creates an integration test class
- **THEN** the package path MUST be `org.smm.archetype.test.cases.integrationtest.<business-module>`
- **THEN** all integration test files MUST be under the integrationtest directory

#### Scenario: 单元测试包路径

- **WHEN** a developer creates a unit test class
- **THEN** the package path MUST be `org.smm.archetype.test.cases.unittest.<business-module>`
- **THEN** all unit test files MUST be under the unittest directory

---

### Requirement: 测试类命名规范化

The system SHALL use unified naming conventions for test classes to quickly identify test types.

#### Scenario: 集成测试命名

- **WHEN** a developer creates an integration test class
- **THEN** the class name MUST end with `ITest` (e.g., `OrderControllerITest`)
- **THEN** no integration test classes SHALL use other naming conventions

#### Scenario: 单元测试命名

- **WHEN** a developer creates a unit test class
- **THEN** the class name MUST end with `UTest` (e.g., `OrderServiceUTest`)
- **THEN** no unit test classes SHALL use other naming conventions

---

### Requirement: 测试基类继承规范化

The system SHALL ensure all test classes inherit from the correct base class to maintain testing behavior consistency.

#### Scenario: 集成测试基类继承

- **WHEN** a developer creates an integration test class
- **THEN** the class MUST inherit from `IntegrationTestBase`
- **THEN** the `IntegrationTestBase` MUST be annotated with `@SpringBootTest`

#### Scenario: 单元测试基类继承

- **WHEN** a developer creates a unit test class
- **THEN** the class MUST inherit from `UnitTestBase`
- **THEN** the unit test MUST NOT start Spring context

---

### Requirement: 测试类型判断规范

The system SHALL determine the correct test type based on the complexity and dependency relationships of the class under test.

#### Scenario: 大入口类使用集成测试

- **WHEN** testing a large entry class (Service, Facade, Controller)
- **THEN** the test MUST be an integration test
- **THEN** the test MUST verify multi-party call coordination

#### Scenario: 简单类使用单元测试

- **WHEN** testing a simple class (utility class, middleware adapter)
- **THEN** the test MUST be a unit test
- **THEN** the test MUST mock all external dependencies

---

### Requirement: 测试规范文档同步

The system SHALL synchronize new testing standards to project documentation to ensure all developers follow unified testing standards.

#### Scenario: 项目规范文档包含测试规范

- **WHEN** a developer reads the project constitution
- **THEN** the document MUST contain the test directory structure standard
- **THEN** the document MUST contain the ITest/UTest naming rules
- **THEN** the document MUST contain the IntegrationTestBase/UnitTestBase usage instructions

---

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

---

