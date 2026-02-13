# DTO Conversion Specification

## ADDED Requirements

### Requirement: MapStruct for all DTO conversions

All DTO conversions SHALL use MapStruct, NOT manual conversion methods or static `fromDTO` methods.

#### Scenario: Infrastructure layer Domain-DO conversion

- **WHEN** converting between Domain objects and DO (Data Objects)
- **THEN** MapStruct `@Mapper` interface MUST be used
- **AND** the converter MUST be placed in `infrastructure/{module}/converter/`

#### Scenario: Adapter layer Request-Command conversion

- **WHEN** converting Request to Command objects
- **THEN** MapStruct `@Mapper` interface MUST be used
- **AND** the converter MUST be placed in `adapter/{module}/converter/`

#### Scenario: Adapter layer DTO-Response conversion

- **WHEN** converting DTO to Response objects
- **THEN** MapStruct `@Mapper` interface MUST be used
- **AND** static `fromDTO` methods MUST be removed

### Requirement: Converter interface structure

MapStruct converters SHALL follow a consistent interface structure.

#### Scenario: Converter naming convention

- **WHEN** creating a converter
- **THEN** the converter name MUST follow pattern `{Entity}Converter`
- **AND** use `@Mapper(componentModel = "spring")`

#### Scenario: Converter method naming

- **WHEN** defining conversion methods
- **THEN** method names MUST follow patterns:
  - `toDTO(domainObject)` - Domain to DTO
  - `toDomain(doObject)` - DO to Domain
  - `toCommand(request)` - Request to Command
  - `toResponse(dto)` - DTO to Response

### Requirement: Enum conversion handling

Enum type conversions SHALL be handled automatically by MapStruct.

#### Scenario: Enum to String conversion

- **WHEN** converting enums to String for persistence
- **THEN** MapStruct SHALL use `enum.name()` automatically
- **AND** no manual conversion code is required

#### Scenario: String to Enum conversion

- **WHEN** converting String to enums from database
- **THEN** MapStruct SHALL use `Enum.valueOf()` automatically
- **AND** null handling MUST be explicit

### Requirement: No manual DTO conversion in services

Application services and controllers SHALL NOT contain manual DTO conversion logic.

#### Scenario: AppService uses converter

- **WHEN** an AppService needs to convert Domain to DTO
- **THEN** it MUST inject the appropriate Converter
- **AND** NOT contain manual builder calls for conversion

#### Scenario: Controller uses converter

- **WHEN** a Controller needs to convert Request to Command
- **THEN** it MUST inject the appropriate Converter
- **AND** NOT contain manual mapping code

### Requirement: Remove static fromDTO methods

Static `fromDTO` methods in Response classes SHALL be removed and replaced with MapStruct converters.

#### Scenario: Response class structure

- **WHEN** defining Response classes
- **THEN** they MUST only contain data fields and Lombok annotations
- **AND** NOT contain static conversion methods
