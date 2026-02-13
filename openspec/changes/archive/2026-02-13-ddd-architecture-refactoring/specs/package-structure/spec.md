# Package Structure Specification

## ADDED Requirements

### Requirement: Standard DDD four-layer package naming

The project SHALL use standardized package naming across all four DDD layers:

| Layer | Package Pattern | Description |
|-------|-----------------|-------------|
| Domain | `org.smm.archetype.domain.{module}` | Pure business logic |
| Application | `org.smm.archetype.app.{module}` | Use case orchestration |
| Infrastructure | `org.smm.archetype.infrastructure.{module}` | Technical implementations |
| Adapter | `org.smm.archetype.adapter.{module}` | External interfaces |

#### Scenario: Package structure follows DDD conventions

- **WHEN** a new module is created
- **THEN** the module MUST be placed in the correct layer package
- **AND** the package structure MUST mirror across all four layers

### Requirement: Shared package naming

Shared infrastructure code SHALL use the `shared` package name instead of `bizshared`.

#### Scenario: Shared package is correctly named

- **WHEN** code is shared across multiple modules within a layer
- **THEN** it MUST be placed in the `shared` subpackage
- **AND** NOT in `bizshared` or `common`

#### Scenario: Shared package structure per layer

- **WHEN** organizing shared code
- **THEN** the following structure MUST be used:
  - `domain/shared/` - Shared domain objects (base classes, exceptions, interfaces)
  - `infrastructure/shared/` - Shared infrastructure (event publishing, serialization)
  - `app/shared/` - Shared application layer (result types, common DTOs)
  - `adapter/shared/` - Shared adapters (filters, exception handlers)

### Requirement: Platform capability package naming

Platform-level capabilities SHALL use the `platform` package name instead of `common`.

#### Scenario: Platform package is correctly named

- **WHEN** a platform capability (file, search, audit) is implemented
- **THEN** it MUST be placed in the `platform` subpackage
- **AND** NOT in `common`

#### Scenario: Platform capabilities structure

- **WHEN** implementing platform capabilities
- **THEN** the following structure MUST be used:
  - `domain/platform/file/` - File domain model
  - `domain/platform/search/` - Search domain model
  - `domain/platform/audit/` - Audit domain model
  - `infrastructure/platform/file/` - File infrastructure
  - `infrastructure/platform/search/` - Search infrastructure

### Requirement: Business module naming

Business modules SHALL use descriptive business domain names, not generic names like `example`.

#### Scenario: Module name reflects business domain

- **WHEN** a business module is created
- **THEN** the module name MUST clearly describe the business domain
- **AND** NOT use generic names like `example`, `test`, or `demo`

#### Scenario: Example module renamed

- **WHEN** the `example` module exists
- **THEN** it MUST be renamed to `exampleorder` to reflect it is an order domain example
- **AND** all references MUST be updated accordingly
