# START/CONFIG LAYER

**Generated**: 2026-02-01 15:42:00
**Commit**: latest
**Branch**: main

## OVERVIEW

All Spring Boot configuration classes centralized in start/config/ - Bean assembly, transaction management, middleware auto-detection.

## STRUCTURE

```
start/src/main/java/org/smm/archetype/config/
├── AppConfigure.java          # Transaction management (PlatformTransactionManager, TransactionTemplate)
├── WebConfigure.java          # Filter registration (ContextFillFilter)
├── CacheConfigure.java        # Cache client selection (Redis/Caffeine auto-detection)
├── DomainEventConfigure.java # Event publisher/consumer selection
├── SearchConfigure.java       # Elasticsearch service configuration
├── ScheduleConfigure.java       # Event retry scheduler
├── NotificationConfigure.java  # SMS/Email clients (Aliyun)
├── ThreadPoolConfigure.java   # 4 thread pools (IO/CPU/Virtual/Daemon)
├── OssConfigure.java          # Object storage (RustFS/Local)
├── LogConfigure.java           # Logging configuration
└── EventKafkaConfigure.java   # Kafka-specific event handling
```

## WHERE TO LOOK

| Task                 | Location                                             | Notes                                                      |
|----------------------|------------------------------------------------------|------------------------------------------------------------|
| Bean assembly        | All *Configure classes                               | @Configuration classes define all application beans        |
| Transaction config   | AppConfigure.java                                    | @EnableTransactionManagement, DataSourceTransactionManager |
| Middleware selection | CacheConfigure, SearchConfigure, EventKafkaConfigure | @ConditionalOnBean for auto-detection                      |
| Thread pools         | ThreadPoolConfigure.java                             | IO/CPU/Virtual/Daemon 4 separate pools                     |
| Entry point          | ApplicationBootstrap.java                            | @SpringBootApplication + CommandLineRunner                 |

## CONVENTIONS

- **Centralized Configuration**: All @Configuration classes MUST be in start/config/ (FORBIDDEN in other modules)
- **Naming Convention**: {Aggregate}Configure (e.g., OrderConfigure, CacheConfigure)
- **Non-Standard Suffix**: Use `Configure` not `Config` (intentional deviation)
- **Bean Assembly**: Only via @Bean methods (FORBIDDEN: @Component, @Service, @Repository scanning)
- **Injection**: Constructor injection with @RequiredArgsConstructor (FORBIDDEN: @Autowired field injection)
- **Cross-Config Dependencies**: @Bean method parameters for same-config dependencies
- **Cross-Config Circular Deps**: Optional + @ConditionalOnBean (FORBIDDEN: @Lazy, ObjectProvider)
- **Auto-Detection Pattern**: Local implementation as default, external via @ConditionalOnBean + @Primary

## ANTI-PATTERNS (THIS LAYER)

- ❌ Configuration classes in adapter/infrastructure/app/domain modules
- ❌ @Component, @Service, @Repository for business beans
- ❌ @Autowired field injection in @Configuration classes
- ❌ @Lazy or ObjectProvider for circular dependencies (must refactor instead)
- ❌ Naming with *Config suffix (must use *Configure)

## UNIQUE STYLES

- **Middleware Auto-Switching**: Redis → Caffeine, Kafka → Spring Events, ES → Disabled
- **Virtual Thread Support**: EnhanceVirtualThreadTaskExecutor for JDK 25 virtual threads
- **External Scheduler Support**: ExternalSchedulerRetryStrategy for XXL-JOB/PowerJob integration
- **Filter Chain**: ContextFillFilter for user ID/domain event injection
- **Profile-Based Properties**: Middleware properties in application.yaml, detected via @ConditionalOnProperty
- **Exec JAR Classification**: start-1.0.0-exec.jar for deployment (keeps original JAR for library use)

## MIDDLEWARE CONFIGURATION PATTERNS

| Middleware   | Detection Bean          | Fallback Bean                          | Property                                |
|--------------|-------------------------|----------------------------------------|-----------------------------------------|
| Cache        | RedisTemplate           | CaffeineCacheClientImpl                | middleware.cache.enabled                |
| Event        | KafkaTemplate           | SpringEventPublisherImpl               | middleware.event.kafka.enabled          |
| Search       | ElasticsearchOperations | DisabledSearchClientImpl               | middleware.search.elasticsearch.enabled |
| OSS          | S3Client (RustFS)       | LocalOssClientImpl                     | middleware.oss.rustfs.enabled           |
| Notification | (None - always Aliyun)  | AliyunSmsClientImpl, AliyunEmailClient | middleware.notification.aliyun.enabled  |

## THREAD POOL CONFIGURATIONS

| Pool    | Purpose                | Core Size     | Max Size  | Queue Size | Thread Name Prefix |
|---------|------------------------|---------------|-----------|------------|--------------------|
| IO      | Blocking operations    | CPU cores     | 200       | 1000       | io-pool-           |
| CPU     | Computation tasks      | CPU cores × 2 | 200       | 1000       | cpu-pool-          |
| Virtual | JDK 25 virtual threads | Unlimited     | Unlimited | 1000       | virtual-pool-      |
| Daemon  | Background tasks       | CPU cores / 2 | 200       | 1000       | daemon-pool-       |

## STARTUP PROCESS

1. ApplicationBootstrap.java: @SpringBootApplication with @MapperScan, @EnableAsync, @EnableScheduling
2. Component scan: org.smm.archetype (all modules)
3. Configuration classes: All @Configuration in start/config/ auto-detected and loaded
4. CommandLineRunner.run(): Prints startup URL, port, and OpenAPI path
