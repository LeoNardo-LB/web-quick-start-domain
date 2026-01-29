# START MODULE

Spring Boot bootstrap - bean assembly, configuration, entry point.

## STRUCTURE
```
start/
├── config/          # All @Configuration classes
│   ├── AppConfigure.java          # App-layer beans
│   ├── EventConfigure.java        # Event publisher/consumer
│   ├── CacheConfigure.java        # Cache client selection
│   ├── SearchConfigure.java       # Elasticsearch
│   ├── OssConfigure.java         # Object storage
│   ├── NotificationConfigure.java  # SMS/Email
│   ├── ThreadPoolConfigure.java   # Thread pools (IO/CPU/Daemon)
│   ├── properties/              # @ConfigurationProperties
│   └── condition/               # @Conditional classes
└── ApplicationBootstrap.java      # Main class, CommandLineRunner
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Main Entry | start/.../ApplicationBootstrap.java | Spring Boot main, prints startup URL |
| Bean Assembly | start/src/main/java/org/smm/archetype/config/ | All *Configure classes |
| Config Properties | start/.../config/properties/*.java | @ConfigurationProperties |
| Conditional Logic | start/.../config/condition/*.java | Aliyun enabled conditions |

## CONVENTIONS
- **All @Configuration classes MUST be here** (no config in other modules)
- Naming: `{Aggregate}Configure` (non-standard, intentional)
- Bean assembly via `@Bean` methods only
- Injection: `@RequiredArgsConstructor` for constructor injection
- Cross-config deps: `@Bean` method parameters
- Circular deps: Optional/`@ConditionalOnBean` (no `@Lazy`)

## ANTI-PATTERNS
- ❌ Configuration classes in adapter/infrastructure modules
- ❌ `@Lazy` or `ObjectProvider` for circular deps (must refactor)
- ❌ `@Component` scanning for beans (use `@Bean`)
- ❌ Naming with `*Config` (use `*Configure`)
