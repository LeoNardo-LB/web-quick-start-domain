// package org.smm.archetype.infrastructure.shared.client.cache;
//
// import lombok.extern.slf4j.Slf4j;
// import org.smm.archetype.domain.shared.client.CacheClient;
// import org.smm.archetype.infrastructure.shared.client.cache.AbstractCacheClient;
// import org.springframework.data.redis.core.RedisTemplate;
//
// import java.time.Duration;
// import java.util.List;
// import java.util.concurrent.TimeUnit;
// import java.util.stream.Collectors;
//
// /**
//  * Redis缓存实现，基于Fastjson2序列化，适用于分布式场景。
//  */
// @Slf4j
// public class RedisCacheClientImpl extends AbstractCacheClient {
//
//     private final RedisTemplate<String, Object> redisTemplate;
//
//     public RedisCacheClientImpl(RedisTemplate<String, Object> redisTemplate) {
//         this.redisTemplate = redisTemplate;
//         log.info("Redis缓存初始化成功（分布式缓存）: RedisTemplate={}", redisTemplate);
//     }
//
//     // ==================== 扩展点实现（protected do* 方法） ====================
//
//     @Override
//     protected <T> T doGet(String key) throws Exception {
//         Object value = redisTemplate.opsForValue().get(key);
//         if (value == null) {
//             return null;
//         }
//         // 由于使用了WriteClassName特性，Fastjson2会自动反序列化为正确的类型
//         return (T) value;
//     }
//
//     @Override
//     protected <T> List<T> doGetList(String key) throws Exception {
//         List<Object> range = redisTemplate.opsForList().range(key, 0, -1);
//         if (range == null || range.isEmpty()) {
//             return List.of();
//         }
//         // 直接转换，Fastjson2已经处理了类型信息，零拷贝高性能
//         return range.stream()
//                        .map(item -> (T) item)
//                        .collect(Collectors.toList());
//     }
//
//     @Override
//     protected <T> List<T> doGetList(String key, int beginIdx, int endIdx) throws Exception {
//         List<Object> range = redisTemplate.opsForList().range(key, beginIdx, endIdx);
//         if (range == null || range.isEmpty()) {
//             return List.of();
//         }
//         // 直接转换，Fastjson2已经处理了类型信息，零拷贝高性能
//         return range.stream()
//                        .map(item -> (T) item)
//                        .collect(Collectors.toList());
//     }
//
//     @Override
//     protected void doPut(String key, Object value) throws Exception {
//         redisTemplate.opsForValue().set(key, value);
//     }
//
//     @Override
//     protected void doPut(String key, Object value, Duration duration) throws Exception {
//         redisTemplate.opsForValue().set(key, value, duration);
//     }
//
//     @Override
//     protected void doAppend(String key, Object value) throws Exception {
//         redisTemplate.opsForList().rightPush(key, value);
//     }
//
//     @Override
//     protected void doDelete(String key) throws Exception {
//         redisTemplate.delete(key);
//     }
//
//     @Override
//     protected Boolean doHasKey(String key) throws Exception {
//         return redisTemplate.hasKey(key);
//     }
//
//     @Override
//     protected Boolean doExpire(String key, long timeout, TimeUnit unit) throws Exception {
//         return redisTemplate.expire(key, timeout, unit);
//     }
//
//     @Override
//     protected Long doGetExpire(String key) throws Exception {
//         return redisTemplate.getExpire(key, TimeUnit.SECONDS);
//     }
//
// }
