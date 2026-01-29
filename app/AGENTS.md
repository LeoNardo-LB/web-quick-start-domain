# APP LAYER

Application orchestration - CQRS, transaction boundaries, use case coordination.

## STRUCTURE
```
app/
├── bizshared/       # Shared application services
│   ├── result/      # BaseResult, PageResult wrappers
│   └── query/       # Query service base classes
└── _example/        # Example application service
    ├── OrderAppService.java
    ├── command/      # CQRS Commands (CreateOrderCommand, etc.)
    ├── query/        # CQRS Queries (OrderQuery, etc.)
    └── dto/         # App DTOs (OrderDTO, MoneyDTO)
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Application Services | app/**/*AppService.java | Use case orchestration |
| CQRS Commands | app/**/command/ | Input DTOs for write ops |
| CQRS Queries | app/**/query/ | Input DTOs for read ops |
| App DTOs | app/**/dto/ | Data transfer objects |
| Result Wrappers | app/bizshared/result/ | BaseResult, PageResult |

## CONVENTIONS
- **Orchestration only** - NO business rules (domain enforces invariants)
- Transaction boundaries: `@Transactional` on ApplicationService methods
- CQRS: Separate Command/Query DTOs and handlers
- MapStruct for App DTO ↔ Domain/Response DTO conversion
- Coordinate multiple aggregates/services

## ANTI-PATTERNS
- ❌ Business rules (move to Domain aggregates)
- ❌ Direct infrastructure access (use Repository interfaces from domain)
- ❌ Domain objects exposed to adapters (convert via DTOs)
- ❌ Configuration classes (must be in start/)
- ❌ `@Data` annotation
