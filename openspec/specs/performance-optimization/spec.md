# performance-optimization Specification

## Purpose
TBD - created by archiving change migrate-spec-kit-specs-to-opencode. Update Purpose after archive.
## Requirements
### Requirement: N+1查询识别和解决

The system MUST identify and resolve N+1 query problems to avoid database access performance bottlenecks.

#### Scenario: 使用MyBatis-Flex批量查询

- **WHEN** 开发者需要查询多个订单（基于订单ID列表）
- **THEN** 必须使用MyBatis-Flex的批量查询方法（selectBatchByIds）
- **THEN** 禁止在循环中执行单条查询（N+1问题）
- **THEN** 批量查询的结果集大小必须有上限（默认100条），超过上限必须分页

#### Scenario: 使用JOIN优化关联查询

- **WHEN** 开发者需要查询订单及其关联的用户信息
- **THEN** 必须使用SQL JOIN在一次查询中获取所有必要数据
- **THEN** 禁止先查询订单列表，再循环查询每个订单的用户信息
- **THEN** JOIN的字段必须包含在SELECT列表中，禁止 `SELECT *`
- **THEN** 对于多对多关联，必须考虑使用LEFT JOIN和唯一索引

#### Scenario: 使用IN子查询优化

- **WHEN** 开发者需要查询属于特定用户的订单
- **THEN** 必须使用 `WHERE user_id IN (SELECT id FROM users WHERE ...)` 子查询
- **THEN** 禁止先查询用户ID列表，再循环查询订单
- **THEN** IN子查询的参数列表长度必须有限制（默认1000个），超过上限必须分批处理

#### Scenario: 延迟加载（Lazy Loading）合理使用

- **WHEN** 开发者使用JPA/Hibernate的延迟加载
- **THEN** 必须在事务内访问延迟加载的关联对象，避免LazyInitializationException
- **THEN** 对于确定需要的关联数据，必须使用JOIN FETCH（@EntityGraph或JOIN FETCH）
- **THEN** 禁止在视图层（Controller/View）访问延迟加载对象（可能导致N+1问题）

---

### Requirement: 缓存策略

The system MUST use caching appropriately to improve performance while avoiding cache consistency issues.

#### Scenario: Redis缓存使用规范

- **WHEN** 开发者使用Redis作为缓存
- **THEN** 必须使用StringRedisTemplate或RedisTemplate，禁止直接使用Jedis客户端
- **THEN** 缓存key必须命名规范（`模块:资源:标识`，如 `order:info:12345`）
- **THEN** 缓存value必须序列化（使用JSON或二进制序列化）
- **THEN** 缓存key必须设置合理的过期时间（根据业务调整，避免永久缓存）

#### Scenario: 缓存失效策略

- **WHEN** 数据更新时
- **THEN** 必须同步失效相关缓存（删除或更新）
- **THEN** 禁止让缓存自然过期（可能导致数据不一致）
- **THEN** 对于热点数据，可以使用Cache Aside模式（更新数据库时同步更新缓存）
- **THEN** 对于多级缓存（Redis + 本地缓存），必须保证失效的顺序性（先失效本地，再失效Redis）

#### Scenario: 缓存穿透防护

- **WHEN** 查询不存在的数据（缓存和数据库都没有）
- **THEN** 必须使用空值缓存（缓存key对应null或特殊标记）
- **THEN** 空值缓存必须有较短的过期时间（默认5分钟）
- **THEN** 对于恶意查询大量不存在的key，必须使用Bloom Filter进行预判

#### Scenario: 缓存雪崩防护

- **WHEN** 大量缓存key同时过期（如凌晨0点）
- **THEN** 必须在过期时间上增加随机偏移量（如±10%）
- **THEN** 必须使用多级缓存（本地缓存作为一级，Redis作为二级）
- **THEN** 必须使用熔断机制，防止缓存失效时数据库压力过大

---

### Requirement: 数据库索引设计

The system MUST design reasonable database indexes to optimize query performance.

#### Scenario: 索引选择原则

- **WHEN** 开发者设计数据库表或添加查询
- **THEN** 必须为WHERE条件、JOIN字段、ORDER BY字段创建索引
- **THEN** 索引字段的选择性（distinct ratio）必须高于10%（如性别字段不建索引）
- **THEN** 禁止为低选择性的字段创建索引（浪费磁盘空间和写入性能）

#### Scenario: 复合索引设计

- **WHEN** 查询包含多个条件的组合（如 `WHERE user_id = ? AND status = ?`）
- **THEN** 必须创建复合索引，字段顺序符合最左前缀原则
- **THEN** 索引字段顺序必须按照查询频率和选择性排列
- **THEN** 避免创建冗余的复合索引（如已有A+B，不需要单独创建A）

#### Scenario: 索引优化检查

- **WHEN** 系统运行一段时间后（如1个月）
- **THEN** 必须分析慢查询日志（MySQL slow_query_log）
- **THEN** 对于执行时间超过100ms的查询，必须执行EXPLAIN分析
- **THEN** 必须检查是否使用了索引，如果没有则优化
- **THEN** 必须定期删除未使用的索引（减少写入开销）

---

### Requirement: 慢查询监控

The system MUST monitor slow queries to detect performance issues in a timely manner.

#### Scenario: 慢查询日志配置

- **WHEN** 系统使用MySQL数据库
- **THEN** 必须启用慢查询日志（long_query_time = 1，单位：秒）
- **THEN** 必须记录慢查询的SQL、执行时间、扫描行数
- **THEN** 必须设置慢查询日志轮转（避免磁盘占满）
- **THEN** 慢查询日志必须包含Trace ID，便于关联业务上下文

#### Scenario: 慢查询分析

- **WHEN** 发现慢查询（执行时间超过100ms）
- **THEN** 必须使用EXPLAIN命令分析查询执行计划
- **THEN** 必须检查是否使用了全表扫描（type=ALL）
- **THEN** 必须检查是否使用了临时表或文件排序（Using temporary; Using filesort）
- **THEN** 优化后必须重新EXPLAIN验证，确保执行计划正确

#### Scenario: 慢查询告警

- **WHEN** 慢查询数量超过阈值（默认10次/分钟）
- **THEN** 必须发送告警通知给开发团队
- **THEN** 告警必须包含慢查询SQL、执行时间、请求URL
- **THEN** 慢查询告警必须区分级别（如100ms-500ms为警告，>500ms为严重）

---

### Requirement: 异步处理

The system MUST use asynchronous processing appropriately to improve response speed and throughput.

#### Scenario: @Async使用规范

- **WHEN** 开发者使用Spring的@Async注解
- **THEN** 必须配置独立的线程池（@Async("taskExecutor")）
- **THEN** 禁止使用默认的SimpleAsyncTaskExecutor（无界队列，可能导致OOM）
- **THEN** 线程池配置必须包含核心线程数、最大线程数、队列容量、拒绝策略
- **THEN** 异步方法必须返回Future或CompletableFuture，便于调用方获取结果

#### Scenario: 线程池配置

- **WHEN** 配置异步任务线程池
- **THEN** 核心线程数必须设置为CPU核心数（Runtime.getRuntime().availableProcessors()）
- **THEN** 最大线程数必须设置为核心线程数的2-4倍（根据任务类型IO密集/CPU密集调整）
- **THEN** 队列容量必须合理设置（默认1000），队列满时必须使用CallerRunsPolicy拒绝策略
- **THEN** 线程名必须有意义（如 "order-task-async"），便于日志追踪）

#### Scenario: 异步任务监控

- **WHEN** 执行异步任务
- **THEN** 必须记录任务提交时间和完成时间
- **THEN** 如果异步任务失败，必须记录异常堆栈
- **THEN** 如果队列堆积（等待数量>队列容量的80%），必须发送告警
- **THEN** 异步任务必须支持重试机制（对于可重试的异常）

#### Scenario: 事件驱动异步

- **WHEN** 领域事件触发后需要异步处理（如发送通知、更新报表）
- **THEN** 必须使用Spring的ApplicationEventPublisher
- **THEN** 监听器必须使用@Async注解（@EventListener + @Async）
- **THEN** 监听器必须处理异常，避免事件广播失败影响主流程
- **THEN** 事件处理必须有幂等性保证（重复事件不重复处理）

---

