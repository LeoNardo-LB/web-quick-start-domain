# DOMAIN LAYER

Core DDD business logic - pure domain models with no external dependencies.

## STRUCTURE
```
domain/
├── bizshared/       # Shared domain base classes
│   ├── base/        # AggregateRoot, Entity, DomainEvent, Identifier
│   ├── client/      # Technical client interfaces
│   └── event/       # Domain event base
├── common/          # Reusable domain concepts
│   ├── file/        # File metadata value objects
│   └── search/      # Search abstractions
└── example/         # Example domain (Order aggregate)
    └── order/       # OrderAggr, OrderItem, Money VOs
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Aggregates | domain/**/model/*Aggr.java | Aggregate roots with business rules |
| Value Objects | domain/**/model/valueobject/ | Immutable, no identity |
| Domain Events | domain/**/model/event/ | DomainEvent subclasses |
| Repository Interfaces | domain/**/repository/ | NO implementations here |
| Domain Services | domain/**/service/ | Cross-aggregate logic |

## CONVENTIONS
- **NO external dependencies** - pure business logic only
- Repository: interfaces only, implementations in infrastructure/
- Aggregates: protect invariants via public methods
- Events: recorded via `recordEvent()` in AggregateRoot
- `@Data` FORBIDDEN - use `@Getter`, `@Setter`, `@Builder`

## ANTI-PATTERNS
- ❌ External libs in domain layer (Spring, MyBatis, etc.)
- ❌ Repository implementations (only interfaces)
- ❌ Database entities mixed with domain models
- ❌ `@Data` annotation
