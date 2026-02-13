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

