# Exception Handling Specification

## ADDED Requirements

### Requirement: Domain layer exception types

Domain layer code SHALL use `BizException` for business validation failures, NOT `IllegalArgumentException` or `IllegalStateException`.

#### Scenario: Business rule validation throws BizException

- **WHEN** a business rule validation fails in domain layer
- **THEN** the code MUST throw `BizException` with appropriate error code
- **AND** NOT throw `IllegalArgumentException` or `IllegalStateException`

#### Scenario: Invalid order state transition

- **WHEN** attempting to pay an order that is not in CREATED status
- **THEN** `BizException` MUST be thrown with error code `OrderErrorCode.INVALID_STATUS`
- **AND** the exception message MUST describe the current and expected states

### Requirement: Infrastructure layer exception types

Infrastructure layer code SHALL use `SysException` for technical failures, NOT `RuntimeException`.

#### Scenario: Technical failure throws SysException

- **WHEN** a technical failure occurs (database, network, external service)
- **THEN** the code MUST throw `SysException` with appropriate error code
- **AND** NOT throw generic `RuntimeException`

#### Scenario: Database connection failure

- **WHEN** database connection fails
- **THEN** `SysException` MUST be thrown with error code indicating database failure
- **AND** the original exception MUST be wrapped as the cause

### Requirement: Client exception for external services

Code calling external client services SHALL use `ClientException` for client-side failures.

#### Scenario: External service call failure

- **WHEN** an external service call fails (SMS, Email, OSS)
- **THEN** `ClientException` MUST be thrown with `ClientErrorCode`
- **AND** the client name and operation MUST be included in the error message

### Requirement: Exception hierarchy

All custom exceptions SHALL extend from `BaseException`.

#### Scenario: Custom exception extends BaseException

- **WHEN** creating a new exception type
- **THEN** it MUST extend `BaseException`, `BizException`, `SysException`, or `ClientException`
- **AND** NOT extend `RuntimeException` or `Exception` directly

### Requirement: Error code enumeration

Error codes SHALL be defined as enums with descriptive messages.

#### Scenario: Error code enum structure

- **WHEN** defining error codes
- **THEN** each error code MUST have:
  - A unique code (e.g., "ORDER_001")
  - A descriptive message
  - Optional HTTP status code mapping

#### Scenario: ErrorCode enum usage

- **WHEN** throwing an exception
- **THEN** the error code enum MUST be passed to the exception constructor
- **AND** NOT use string literals for error codes
