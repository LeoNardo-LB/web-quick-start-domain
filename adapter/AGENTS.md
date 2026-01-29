# ADAPTER LAYER

Interface adapters - REST controllers, event listeners, scheduled tasks.

## STRUCTURE
```
adapter/
├── _example/        # Example controllers
│   └── web/
│       └── api/     # OrderController
├── listener/        # Event listener implementations
│   ├── SpringEventListener.java
│   └── KafkaEventListener.java
├── schedule/        # Event retry schedulers
│   ├── EventRetryScheduler.java
│   ├── ExponentialBackoffRetryStrategy.java
│   └── ExternalSchedulerRetryStrategy.java
├── web/            # Additional web controllers
└── bizshared/       # Shared adapter utilities
    └── util/       # IpUtils, etc.
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| REST Controllers | adapter/**/web/api/*Controller.java | Endpoints, delegate to App layer |
| Event Listeners | adapter/listener/ | Spring Events & Kafka consumers |
| Retry Schedulers | adapter/schedule/ | Event retry with strategies |
| Request DTOs | adapter/**/dto/request/ | Input validation |
| Response DTOs | adapter/**/dto/response/ | Output formatting |

## CONVENTIONS
- NO business logic - delegate to Application layer
- Controllers: `@RestController`, return `Response<T>` wrapper
- Event listeners: auto-detected via `@ConditionalOnBean`
- Retry strategies: Exponential backoff, external scheduler (XXL-JOB/PowerJob)
- MapStruct for DTO ↔ App DTO conversion

## ANTI-PATTERNS
- ❌ Business rules in controllers (move to Domain)
- ❌ Direct repository calls (use AppService)
- ❌ Configuration classes (must be in start/)
- ❌ Domain objects in DTOs (convert via App layer)
