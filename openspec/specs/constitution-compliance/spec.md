# constitution-compliance Specification

## Purpose
TBD - created by archiving change convert-existing-specs-to-openspec. Update Purpose after archive.
## Requirements
### Requirement: 修复高内聚原则违规

The system SHALL fix violations of the XXIV. High Cohesion Principle, including public inner classes, inner enums, and unnecessary public visibility.

#### Scenario: 内部类可见性修复

- **WHEN** inner classes (OrderItemRequest, AddressRequest, ContactInfoRequest) are public
- **THEN** they MUST be changed to private static
- **THEN** inner classes MUST NOT be accessed from outside

#### Scenario: 内部枚举可见性修复

- **WHEN** enums in FileBusiness and FileMetadata are public
- **THEN** they MUST be changed to package-private
- **THEN** enums MUST only be used within related classes

#### Scenario: 配置内部类修复

- **WHEN** inner classes in ThreadPoolProperties (Io, Cpu, Daemon, Scheduler) are public
- **THEN** they MUST be changed to private static
- **THEN** configuration internal structure MUST NOT be exposed

---

### Requirement: 修复值对象违规

The system SHALL fix value object implementation violations, including missing equalityFields method, mutable fields, and not inheriting ValueObject base class.

#### Scenario: 实现equalityFields方法

- **WHEN** a value object (OrderItemInfo) inherits ValueObject but does not implement equalityFields()
- **THEN** the equalityFields() method MUST be added
- **THEN** the value object MUST correctly perform equality comparison

#### Scenario: 值对象不可变性

- **WHEN** a value object (Event) contains mutable fields (status, maxRetryTimes)
- **THEN** the object MUST be refactored to immutable or converted to entity
- **THEN** value objects MUST maintain immutability

#### Scenario: 继承ValueObject基类

- **WHEN** classes (SearchFilter, SearchHit, EmailAttachment) do not inherit ValueObject
- **THEN** they MUST inherit ValueObject and use @SuperBuilder
- **THEN** value objects MUST follow unified project patterns

---

### Requirement: 修复仓储违规

The system SHALL fix repository layer architecture issues, including wrong location, non-standard naming, and incomplete implementation.

#### Scenario: 仓储位置修复

- **WHEN** a repository (EventRepository) is in bizshared/event/repository
- **THEN** it MUST be moved to the persistence package
- **THEN** repository location MUST comply with standards

#### Scenario: 仓储命名修复

- **WHEN** a repository implementation (OrderAggrRepositoryImpl) naming is incorrect
- **THEN** it MUST be renamed to OrderRepositoryImpl
- **THEN** repository naming MUST comply with standards

#### Scenario: 仓储实现完整性

- **WHEN** a repository implementation (OrderAggrRepositoryImpl) has empty methods
- **THEN** actual database operations MUST be implemented
- **THEN** repository functionality MUST work correctly

---

### Requirement: 修复Response DTO违规

The system SHALL fix Response DTO class annotation issues to ensure all Response objects use the @Builder(setterPrefix = "set") pattern.

#### Scenario: Response类使用Builder模式

- **WHEN** a Response class (ContactInfoResponse) uses @Getter/@Setter
- **THEN** it MUST be replaced with @Builder(setterPrefix = "set")
- **THEN** Response MUST follow unified pattern

#### Scenario: fromDTO方法使用Builder模式

- **WHEN** fromDTO method uses setter instead of Builder
- **THEN** it MUST be refactored to Builder pattern
- **THEN** DTO conversion logic MUST comply with standards

---

### Requirement: 修复事务管理违规

The system SHALL fix transaction management violations by removing @Transactional annotations from Infrastructure layer and adding them to Application layer.

#### Scenario: Infrastructure层移除事务注解

- **WHEN** FileDomainServiceImpl has @Transactional
- **THEN** @Transactional MUST be removed
- **THEN** transaction boundary MUST NOT be in Infrastructure layer

#### Scenario: Application层添加事务注解

- **WHEN** Application layer calls FileDomainService
- **THEN** @Transactional MUST be added to Application methods
- **THEN** transaction management MUST comply with DDD principles

---

