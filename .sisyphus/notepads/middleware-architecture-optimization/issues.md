# Middleware Architecture Optimization - Issues

## Task 2: Refactor SearchConfigure Configuration

### Issue #1: Logback Configuration Error (Pre-existing)

**Description**:
Application startup fails with logback configuration error:
```
ERROR in ch.qos.logback.core.model.processor.AppenderRefModelHandler - Failed to find appender named [CONSOLE]
java.lang.IllegalStateException: Logback configuration error detected
```

**Root Cause**:
- `src/main/resources/logback-spring.xml` references a CONSOLE appender that doesn't exist
- Spring profile elements incorrectly nested within appender/logger/root elements

**Impact**:
- BLOCKS: Startup verification for Task 2
- BLOCKS: All subsequent functional testing tasks

**Status**:
- PRE-EXISTING issue (not caused by Task 2 changes)
- Task 2 compilation succeeds
- Issue unrelated to SearchConfigure refactoring

**Recommended Action**:
- Fix logback-spring.xml configuration to properly define CONSOLE appender
- Ensure spring profile elements are at the correct level in the XML hierarchy
- Separate spring profiles from appender definitions

**Verification**:
- Once logback is fixed, verify Task 2 startup:
  1. Without ES: Should log "Elasticsearch is disabled (ElasticsearchOperations bean not found)"
  2. With ES: Should log "Elasticsearch client initialized with ElasticsearchOperations"


## Task 12: Fix Logback Configuration (Final Fix)

### Issue #2: Spring Profile Nesting Error

**Fixed**: 2026-01-24

**Problem**:
Spring profile elements (`<springProfile>`) were incorrectly nested inside the `<root>` element at lines 66-68, causing logback to fail processing the CONSOLE appender reference.

**Root Cause**:
- Spring profile elements must be at the `<configuration>` level, not inside `<root>` or `<logger>` elements
- Original structure: `<root> <springProfile> <appender-ref ref="CONSOLE"/> </springProfile> </root>`
- This caused warnings: "Ignoring unknown property [appender-ref]" and "Appender named [CONSOLE] not referenced"

**Solution**:
Moved springProfile elements from inside root to configuration level with proper structure:
```xml
<!-- Spring Profile配置（配置级别） -->
<springProfile name="dev,test,default">
    <root level="${LOG_LEVEL}">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ERROR_FILE"/>
        <appender-ref ref="ASYNC_FILE"/>
        <appender-ref ref="ASYNC_CURRENT"/>
    </root>
</springProfile>

<!-- 默认root配置（无profile时） -->
<root level="${LOG_LEVEL}">
    <appender-ref ref="ERROR_FILE"/>
    <appender-ref ref="ASYNC_FILE"/>
    <appender-ref ref="ASYNC_CURRENT"/>
</root>
```

**Changes Made**:
- File: `start/src/main/resources/logback-spring.xml`
- Lines 61-76: Moved springProfile to configuration level
- Preserved all appenders: FILE, ASYNC_FILE, CURRENT, ASYNC_CURRENT, ERROR_FILE
- Preserved Spring Boot default includes (console-appender.xml)
- Created separate root configurations for dev/test/default profiles vs default

**Verification**:
- ✅ No logback warnings on startup
- ✅ CONSOLE appender is now properly referenced
- ✅ Spring profile configuration is at correct level
- ✅ Application attempts startup (blocked by unrelated Spring Boot Actuate dependency issue)

**Note**:
The logback configuration is now correct. Application startup failure is due to missing Spring Boot Actuate classes (`org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups`), which is a separate dependency issue unrelated to the logback fix.

---

## Final Verification: Application Startup Blocked by Actuate Dependency Issue

### Issue #3: Spring Boot Actuate ClassNotFound (Unrelated to Middleware Refactoring)

**Description**:
After fixing logback, application startup fails with:
```
java.lang.NoClassDefFoundError: org/springframework/boot/health/actuate/endpoint/HealthEndpointGroups
Error processing condition on org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration.webEndpointServletHandlerMapping
```

**Root Cause**:
- Spring Boot Actuate dependency missing or version mismatch
- Class `HealthEndpointGroups` not found in classpath
- Likely due to Spring Boot starter dependencies incompatibility

**Impact**:
- BLOCKS: Final verification task "应用启动成功（测试环境配置）"
- Prevents: Startup verification with middleware disabled/enabled
- Note: NOT related to middleware architecture refactoring

**Status**:
- Middleware refactoring: ALL TASKS COMPLETE (11/11)
- Logback configuration: FIXED
- Compilation: SUCCESS
- Startup: BLOCKED by Actuate dependency issue (unrelated to refactoring)

**Recommended Action**:
- Add or fix `spring-boot-starter-actuator` dependency in pom.xml
- Ensure Spring Boot version compatibility across all dependencies
- OR disable actuator auto-configuration if not needed for this project

**Verification** (pending Actuate fix):
Once Actuate is fixed, verify final middleware architecture:
1. Without middleware: Should use local components (Caffeine, Spring Events, Local OSS, DisabledSearch)
2. With middleware: Should use middleware (Redis, Kafka, RustFS, Elasticsearch)
3. Check logs for correct Bean initialization messages
4. Verify @Primary priority works correctly


---

## Issue #4: Spring Boot Actuator Version Mismatch (FIXED)

**Fixed**: 2026-01-24

**Problem**:
Application startup fails with NoClassDefFoundError for HealthEndpointGroups class.

**Root Cause**:
- Spring Boot parent version: 4.0.2 (correct)
- Spring Boot starter dependencies: Defined in root pom.xml dependencyManagement WITHOUT explicit versions
- When dependencies are in dependencyManagement without versions, they override parent's version management
- Maven cannot resolve version → NoClassDefFoundError: HealthEndpointGroups

**Solution**:
Removed three Spring Boot starter dependencies from root pom.xml dependencyManagement section (lines 287-299):
1. spring-boot-starter-web
2. spring-boot-starter-actuator  
3. spring-boot-starter-data-redis

Let Spring Boot parent 4.0.2 manage versions automatically.

**Changes Made**:
- File: `pom.xml` (root)
- Location: Lines 287-299 in dependencyManagement section
- Action: Removed 3 <dependency> blocks that lacked explicit versions
- Result: Spring Boot parent 4.0.2 now manages these dependencies automatically

**Verification**:
- ✅ Compilation succeeds: `mvn clean compile` no errors
- ✅ Dependency tree confirms: spring-boot-starter-actuator now at version 4.0.2
- ✅ NoClassDefFoundError resolved: HealthEndpointGroups class now available
- ✅ Original Actuator dependency issue: FIXED

**Note**:
This fix resolves the startup verification blocker. Application may still fail with unrelated configuration errors (e.g., ObjectMapper), but the NoClassDefFoundError: HealthEndpointGroups issue is completely resolved.

---

## Issue #5: ObjectMapper UnsatisfiedDependencyException (FIXED)

**Fixed**: 2026-01-24

**Problem**:
Application startup fails with UnsatisfiedDependencyException for ObjectMapper:
```
UnsatisfiedDependencyException: Error creating bean with name 'searchService':
Unsatisfied dependency expressed through method 'searchService' parameter 1:
No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper' available
```

**Root Cause**:
- SearchConfigure class had `private final ObjectMapper objectMapper;` field injection
- searchService() bean method used `objectMapper` in constructor: `new SearchServiceImpl(searchClient, objectMapper)`
- ObjectMapper field was being injected by @RequiredArgsConstructor but there was no @Bean method to create ObjectMapper
- Spring Boot should auto-configure ObjectMapper, but it wasn't being provided as a bean in the SearchConfigure context

**Solution**:
1. Removed `private final ObjectMapper objectMapper;` field from SearchConfigure (line 49)
2. Added explicit `@Bean` method for ObjectMapper creation (lines 51-54):
   ```java
   @Bean
   public ObjectMapper objectMapper() {
       return new ObjectMapper();
   }
   ```
3. Updated searchService() method signature to accept ObjectMapper as parameter (line 71-73):
   ```java
   @Bean
   public SearchService searchService(SearchClient searchClient, ObjectMapper objectMapper) {
       return new SearchServiceImpl(searchClient, objectMapper);
   }
   ```

**Changes Made**:
- File: `start/src/main/java/org/smm/archetype/config/SearchConfigure.java`
- Removed: `private final ObjectMapper objectMapper;` field
- Added: `objectMapper()` @Bean method that returns `new ObjectMapper()`
- Updated: `searchService(SearchClient searchClient, ObjectMapper objectMapper)` to accept ObjectMapper parameter
- Result: ObjectMapper is now provided as a bean via Spring container

**Verification**:
- ✅ Compilation succeeds: `mvn clean compile` no errors
- ✅ Application startup proceeds past SearchConfigure bean creation
- ✅ ObjectMapper UnsatisfiedDependencyException: RESOLVED
- ⚠️ Application now fails on MyBatis configuration (sqlSessionFactory) - separate issue

**Key Insight**:
- Spring Boot auto-configures Jackson ObjectMapper, but it needs to be registered as a @Bean in the configuration context
- Method parameter injection (@Bean method parameters) is the standard Spring Boot pattern for providing dependencies to other beans
- This eliminates the need for field-level dependency injection when a bean is only used in one method

**Note**:
This fix resolves the ObjectMapper dependency issue. Application may still fail with unrelated configuration errors (e.g., MyBatis sqlSessionFactory), but the ObjectMapper UnsatisfiedDependencyException issue is completely resolved.
