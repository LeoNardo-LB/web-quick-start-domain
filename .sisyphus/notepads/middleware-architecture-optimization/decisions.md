# Middleware Architecture Optimization - Decisions

## Task 2: Refactor SearchConfigure Configuration

### Decision #1: Use @ConditionalOnBean Instead of @ConditionalOnProperty

**Context**:
- Old approach: Explicit `middleware.search.enabled=true/false` property
- New requirement: Auto-detect middleware availability

**Decision**:
- Use `@ConditionalOnBean(ElasticsearchOperations.class)` for ES client
- Use `@ConditionalOnMissingBean(ElasticsearchOperations.class)` for disabled client

**Rationale**:
1. **Automatic Detection**: Spring Boot auto-configures ElasticsearchOperations when ES is available
2. **No Manual Configuration**: Users don't need to set `middleware.search.enabled`
3. **Follows New Pattern**: Aligns with "dependency exists → use middleware" rule
4. **Simpler Configuration**: Only need `spring.elasticsearch.*` standard config

**Trade-offs**:
- ✅ Pro: Eliminates explicit enable/disable configuration
- ✅ Pro: Automatic based on dependency presence
- ⚠️ Con: Less explicit control (can't force-disable with property)

### Decision #2: Inject ElasticsearchOperations (Not ElasticsearchClient)

**Context**:
- Task 1 already refactored ElasticsearchClientImpl to use ElasticsearchOperations
- SearchConfigure bean creation must match

**Decision**:
- Inject `ElasticsearchOperations` into esClient() bean
- Pass to ElasticsearchClientImpl constructor

**Rationale**:
1. **Consistency**: Matches Task 1 refactoring
2. **Spring Boot Integration**: Uses auto-configured ElasticsearchRestTemplate
3. **Higher Abstraction**: ElasticsearchOperations provides Spring Data ES API

**Before**:
```java
@Bean
@ConditionalOnProperty(...)
public SearchClient esClient(ElasticsearchClient client) {
    return new ElasticsearchClientImpl(client);
}
```

**After**:
```java
@Bean
@ConditionalOnBean(ElasticsearchOperations.class)
public SearchClient esClient(ElasticsearchOperations operations) {
    return new ElasticsearchClientImpl(operations);
}
```

### Decision #3: Comprehensive JavaDoc Documentation

**Context**:
- New conditional assembly logic needs clear documentation
- Future maintainers need to understand bean selection rules

**Decision**:
- Add detailed class-level JavaDoc explaining:
  - Bean creation logic (ElasticsearchOperations presence)
  - Conditional assembly rules (annotations used)
  - Configuration methods (spring.elasticsearch.* vs disabled)

**Rationale**:
1. **Self-Documenting**: Clear JavaDoc reduces questions
2. **Developer Experience**: Easy to understand without reading code
3. **Pattern Reference**: Documents new architectural pattern for other middleware

### Decision #4: Keep SearchProperties and SearchService Unchanged

**Context**:
- Task specifies not to modify these components
- Future tasks handle SearchProperties simplification

**Decision**:
- Retain `@EnableConfigurationProperties(SearchProperties.class)`
- Keep `searchProperties` field (unused but harmless)
- Keep `searchService()` bean unchanged

**Rationale**:
1. **Task Boundaries**: Task 7 handles SearchProperties simplification
2. **No Breaking Changes**: Keeps configuration backward compatible
3. **Clean Separation**: Each task has clear responsibility

**Future State (Task 7)**:
- Remove `enabled` field from SearchProperties
- Remove `Elasticsearch` inner class
- Potentially remove unused searchProperties field

### Decision #5: No @Primary Annotation

**Context**:
- Other middleware (Cache, OSS) use @Primary for middleware implementations
- Search only has 2 implementations: ElasticsearchClientImpl and DisabledSearchClientImpl

**Decision**:
- Do NOT add @Primary to esClient() bean
- Rely on @ConditionalOnBean/@ConditionalOnMissingBean mutual exclusion

**Rationale**:
1. **Mutual Exclusion**: @ConditionalOnBean and @ConditionalOnMissingBean never both create beans
2. **No Ambiguity**: Only one SearchClient bean ever exists
3. **Cleaner Code**: @Primary would be redundant
4. **Simpler Pattern**: Clear which implementation is active based on ES availability

**Comparison**:
```java
// Cache (2 implementations both potentially present)
@Bean @Primary @ConditionalOnBean(RedisTemplate.class)
public CacheClient redisClient() { ... }

@Bean @ConditionalOnMissingBean(RedisTemplate.class)
public CacheClient caffeineCache() { ... }

// Search (only 1 implementation present)
@Bean @ConditionalOnBean(ElasticsearchOperations.class)
public SearchClient esClient() { ... }

@Bean @ConditionalOnMissingBean(ElasticsearchOperations.class)
public SearchClient disabledEsClient() { ... }
```

### Architectural Pattern Established

**Pattern**: Dependency-Based Conditional Assembly

**Template**:
```java
@Bean
@ConditionalOnBean(MiddlewareBean.class)  // Middleware available
public ClientName middlewareClient(MiddlewareBean bean) {
    return new MiddlewareClientImpl(bean);
}

@Bean
@ConditionalOnMissingBean(MiddlewareBean.class)  // Middleware unavailable
public ClientName disabledClient() {
    return new DisabledClientImpl();
}
```

**Applies To**:
- ✅ Search (ElasticsearchOperations)
- ✅ Cache (RedisTemplate)
- ✅ Events (KafkaTemplate)
- ✅ OSS (RustFsOssClientImpl - custom detection)
