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

## DDL Verification Task: Design Patterns Decisions

### Decision #1: No Physical Foreign Keys (DDD Practice)

**Context**:
- event_consume.event_id references event_publish.event_id
- file_business.file_meta_id references file_metadata.id

**Decision**:
- Use logical foreign keys with comments instead of physical foreign key constraints
- Reference: "关联event_publish.event_id（逻辑外键，无物理外键约束）"

**Rationale**:
1. **DDD Best Practice**: Physical foreign keys create tight coupling between aggregates
2. **Performance**: No foreign key constraint checks during inserts/updates
3. **Flexibility**: Easier to restructure aggregates without schema migrations
4. **Clear Documentation**: Comments explain relationships explicitly

**Trade-offs**:
- ✅ Pro: Better performance (no constraint overhead)
- ✅ Pro: More flexible aggregate design
- ✅ Pro: Clear explicit relationships in comments
- ⚠️ Con: Application must enforce referential integrity
- ⚠️ Con: Risk of orphan records if not properly managed

### Decision #2: Logical Deletion Pattern

**Context**:
All tables have delete_time and delete_user fields for soft deletion.

**Decision**:
- Include delete_time (TIMESTAMP NULL DEFAULT NULL) and delete_user (VARCHAR(64)) in all tables
- NULL values indicate record is not deleted
- Non-NULL values indicate soft deletion with timestamp and user ID

**Rationale**:
1. **Data Retention**: Keep deleted records for audit trails
2. **Recovery**: Allow data recovery if needed
3. **Auditing**: Track who deleted when and by whom
4. **Query Performance**: Use index on delete_time for filtering

**Implementation Pattern**:
```sql
-- Logical deletion fields
`delete_time` TIMESTAMP NULL DEFAULT NULL COMMENT '删除时间（逻辑删除）',
`delete_user` VARCHAR(64) DEFAULT NULL COMMENT '删除人ID（逻辑删除）',
```

**Query Pattern**:
```sql
-- Query only non-deleted records
SELECT * FROM table WHERE delete_time IS NULL;

-- Query deleted records
SELECT * FROM table WHERE delete_time IS NOT NULL;
```

### Decision #3: LogDO Special Handling for Deletion Fields

**Context**:
- BaseDO provides delete_time and delete_user (but not used for most tables)
- LogDO has its own deleteTime and deleteUser fields
- Log table should use LogDO's own fields, not BaseDO's

**Decision**:
- Log table has 15 fields: 10 from LogDO + 5 from BaseDO (createTime, updateTime, createUser, updateUser, id)
- delete_time and delete_user in DDL come from LogDO's deleteTime and deleteUser
- Comments clarify: "LogDO自身字段" vs "BaseDO提供"

**Rationale**:
1. **Semantic Clarity**: LogDO's deletion fields have different purpose (log retention)
2. **Field Count**: Log table has 15 fields total (not 17 with duplicate delete_time/delete_user)
3. **Explicit Documentation**: Comments prevent confusion about field source

**DDL Comments**:
```sql
-- 审计字段（来自BaseDO）
`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（BaseDO提供）',
`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（BaseDO提供）',
`create_user` VARCHAR(64) DEFAULT NULL COMMENT '创建人ID（BaseDO提供）',
`update_user` VARCHAR(64) DEFAULT NULL COMMENT '更新人ID（BaseDO提供）',

-- Log表特有的删除字段（LogDO自身提供，不使用BaseDO的delete_time/delete_user）
`delete_time` TIMESTAMP NULL DEFAULT NULL COMMENT '删除时间（LogDO自身字段）',
`delete_user` VARCHAR(64) DEFAULT NULL COMMENT '删除人ID（LogDO自身字段）',
```

### Decision #4: Optimistic Locking with Version Field

**Context**:
Event publishing and consumption require concurrency control.

**Decision**:
- Add version BIGINT field to event_publish, event_consume, and log tables
- Default value: 0
- Use for optimistic concurrency control

**Rationale**:
1. **No Locking**: Avoid database row locks during reads
2. **Performance**: Better than pessimistic locking for read-heavy scenarios
3. **Conflict Detection**: Version mismatch indicates concurrent modification
4. **Event-Driven**: Suitable for event sourcing patterns

**Implementation Pattern**:
```sql
`version` BIGINT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
```

**Application Usage**:
```java
// Update with version check
UPDATE event_publish SET status = 'PUBLISHED', version = version + 1 
WHERE id = ? AND version = ?;

// Check affected rows
if (affectedRows == 0) {
    throw new OptimisticLockException("Concurrent modification detected");
}
```

### Decision #5: Composite Index Field Ordering

**Context**:
log table requires queries by business_type + start_time, and method + start_time.

**Decision**:
- Create composite indexes: (business_type, start_time) and (method, start_time)
- Field ordering: equality condition field first, range condition field last

**Rationale**:
1. **Index Optimization**: MySQL can use equality field from index, then scan range field
2. **Multi-Column Index**: Single index supports both equality and range queries
3. **B-Tree Structure**: Leftmost prefix rule ensures optimal query execution

**DDL Implementation**:
```sql
KEY `idx_log_business_type_start_time` (`business_type`, `start_time`) COMMENT '按业务类型和时间范围查询',
KEY `idx_log_method_start_time` (`method`, `start_time`) COMMENT '按方法名和时间范围查询',
```

**Query Patterns Supported**:
```sql
-- Uses idx_log_business_type_start_time (both fields)
SELECT * FROM log WHERE business_type = 'ORDER' AND start_time BETWEEN ? AND ?;

-- Uses idx_log_business_type_start_time (only first field)
SELECT * FROM log WHERE business_type = 'ORDER';

-- Uses idx_log_start_time (if separate index exists)
SELECT * FROM log WHERE start_time BETWEEN ? AND ?;
```

### Decision #6: Business Unique Constraints

**Context**:
Prevent duplicate data at business level.

**Decision**:
- event_publish: UNIQUE KEY uk_event_publish_event_id (event_id) - prevent duplicate event IDs
- event_consume: UNIQUE KEY uk_event_consume_event_id_consumer_group (event_id, consumer_group) - prevent duplicate consumption
- event_consume: UNIQUE KEY uk_event_consume_idempotent_key (idempotent_key) - idempotency guarantee
- file_metadata: UNIQUE KEY uk_file_metadata_md5 (md5) - deduplication by file hash

**Rationale**:
1. **Data Integrity**: Database enforces business rules at schema level
2. **Performance**: Unique indexes provide fast lookups
3. **Clear Constraints**: Business rules visible in DDL
4. **No Application Code**: Reduces need for application-level checks

**Trade-offs**:
- ✅ Pro: Enforced at database level (cannot bypass)
- ✅ Pro: Fast lookups via unique index
- ⚠️ Con: Violation causes database exception (need try-catch)

### Decision #7: Index Naming Convention

**Context**:
Need consistent naming for easy identification of index types.

**Decision**:
- Unique indexes: uk_表名_字段名
- Normal indexes: idx_表名_字段名
- Composite indexes: Fields separated by underscores

**Rationale**:
1. **Clear Identification**: Prefix shows index type (uk = unique, idx = normal)
2. **Easy Search**: Can quickly find indexes by table name
3. **Documentation**: Index names serve as inline documentation
4. **Maintenance**: Clear naming simplifies index management

**Examples**:
```sql
-- Unique index (uk)
UNIQUE KEY `uk_event_publish_event_id` (`event_id`)

-- Composite unique index
UNIQUE KEY `uk_event_consume_event_id_consumer_group` (`event_id`, `consumer_group`)

-- Normal index (idx)
KEY `idx_event_publish_aggregate_id` (`aggregate_id`)

-- Composite normal index
KEY `idx_log_business_type_start_time` (`business_type`, `start_time`)
```

### Decision #8: Timestamp Auto-Update

**Context**:
All tables need automatic timestamp management for auditing.

**Decision**:
- create_time: DEFAULT CURRENT_TIMESTAMP (set on insert)
- update_time: DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP (set on insert and update)

**Rationale**:
1. **Automatic**: No application code needed for timestamp management
2. **Consistency**: All tables use same pattern
3. **Audit Trail**: Automatic tracking of record lifecycle
4. **Performance**: Uses database's own clock, no application latency

**DDL Implementation**:
```sql
`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAM

## DDL Verification Task: Design Patterns Decisions

### Decision #1: No Physical Foreign Keys (DDD Practice)
All tables use logical foreign keys with comments instead of physical constraints.

### Decision #2: Logical Deletion Pattern
All tables have delete_time and delete_user fields for soft deletion.

### Decision #3: LogDO Special Handling for Deletion Fields
LogDO's deleteTime and deleteUser are own fields, not from BaseDO.

### Decision #4: Optimistic Locking with Version Field
event_publish, event_consume, log tables have version field for concurrency control.

### Decision #5: Composite Index Field Ordering
Equality field first, range field last (e.g., business_type, start_time).

### Decision #6: Business Unique Constraints
Unique indexes enforce business rules at database level.

### Decision #7: Index Naming Convention
uk_table_name_field_name (unique), idx_table_name_field_name (normal).

### Decision #8: Timestamp Auto-Update
create_time: DEFAULT CURRENT_TIMESTAMP, update_time: ON UPDATE CURRENT_TIMESTAMP.
