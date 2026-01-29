# PROJECT KNOWLEDGE BASE

**Generated**: 2026-01-29 21:00:00
**Commit**: latest
**Branch**: main

## OVERVIEW
DDD-compliant Maven multi-module Java project (JDK 25, Spring Boot 4.0.2) implementing four-layer architecture (Domain-App-Infra-Adapter) with CQRS and event-driven patterns.

## STRUCTURE
```
web-quick-start-domain/
├── adapter/          # Interface adapters (Controllers, Listeners, Schedulers)
├── app/             # Application orchestration (CQRS, Commands, Queries)
├── domain/           # Core business logic (Aggregates, Entities, VOs, Events)
├── infrastructure/    # Technical implementations (Repositories, Cache, Search, OSS)
├── start/           # Bootstrap module (Bean assembly via *Configure classes)
├── test/            # Independent test module with startup validation
├── _docs/           # Project documentation and specifications
└── pom.xml          # Root Maven POM
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Entry point | start/src/main/java/org/smm/archetype/ApplicationBootstrap.java | Spring Boot main, CommandLineRunner |
| Domain models | domain/src/main/java/org/smm/archetype/domain/ | Aggregates, Entities, Value Objects |
| Application services | app/src/main/java/org/smm/archetype/app/ | CQRS orchestration |
| Repository implementations | infrastructure/src/main/java/org/smm/archetype/infrastructure/ | MyBatis-Flex mappers |
| Controllers | adapter/src/main/java/org/smm/archetype/adapter/ | REST endpoints |
| Bean configs | start/src/main/java/org/smm/archetype/config/ | All *Configure classes |
| Test validation | test/src/test/java/org/smm/archetype/test/ApplicationStartupTests.java | Startup integration test |

## CODE MAP
(Not available - LSP symbols not queried)

## CONVENTIONS

**Non-standard naming (intentional project style)**:
- Configuration classes: `*Configure` (e.g., `OrderConfigure`, NOT `OrderConfig`)
- Shared packages: `bizshared` (not `shared`/`common`)
- Example code: `_example/` prefix within production modules

**Standard DDD layering**:
- Adapter → Application → Domain ← Infrastructure (dependency rule)
- Domain layer: NO external dependencies, pure business logic
- Repository interfaces in Domain, implementations in Infrastructure

**Configuration rules**:
- All `@Configuration` classes MUST be in `start/src/main/java/org/smm/archetype/config/`
- Naming: `{Aggregate}Configure` (e.g., `OrderConfigure`)
- Bean assembly via `@Bean` methods only (no `@Component` scanning for beans)

**Lombok rules**:
- `@Data`: ❌ FORBIDDEN (uncontrolled code generation)
- Use: `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`

## ANTI-PATTERNS (THIS PROJECT)

**Forbidden patterns**:
1. `@Data` annotation - Uncontrolled equals/hashCode generation
2. `@Lazy`, `ObjectProvider` for circular deps - Must refactor instead
3. Configuration classes outside `start/` module - Only `start/config/` allowed
4. Tests in production modules - Must use separate `test/` module
5. External dependencies in Domain layer - Domain must remain pure

## UNIQUE STYLES

**Event-driven architecture**:
- Auto-detection: Spring Events (default) vs Kafka (conditional on Bean presence)
- Retry strategies: Exponential backoff, External scheduler (XXL-JOB/PowerJob support)

**Test-first validation**:
- Mandatory 4-step verification: compile → test → startup → coverage
- Independent test module with `ApplicationStartupTests` for Spring context validation

**Bean assembly pattern**:
- Constructor injection with `@RequiredArgsConstructor` preferred
- `@Bean` method parameters for intra-config dependencies
- Optional/`@ConditionalOnBean` for cross-config circular deps

## COMMANDS
```bash
# Compile
mvn clean compile

# Test (with JaCoCo coverage, requires JDK 25)
mvn test

# Startup validation (most critical)
mvn test -Dtest=ApplicationStartupTests -pl test

# Run application
mvn spring-boot:run -pl start

# Generate archetype (for new projects)
mvn archetype:generate -DarchetypeGroupId=org.smm.archetype -DarchetypeArtifactId=web-quick-start-domain -DarchetypeVersion=1.0.0
```

## NOTES

**Deviation warnings** (documented in `_docs/specification/业务代码编写规范.md`):
- Spring Boot version mismatch: pom.xml shows 4.0.2, README mentions 3.5.9
- MyBatis-Flex: Manual dependency management instead of spring-boot-starter
- Test structure: Isolated `test/` module (not co-located with production)

**Coverage requirements**:
- Line coverage: ≥95%
- Branch coverage: 100%
- Test pass rate: 100%

**Gotchas**:
- JDK 25 required (virtual threads enabled)
- Middleware optional: Kafka, Redis, Elasticsearch - all auto-detected via `@ConditionalOnBean`
- Example code (`_example/`) is part of production modules, not separate
- Startup validation test is the gatekeeper for Spring context health
