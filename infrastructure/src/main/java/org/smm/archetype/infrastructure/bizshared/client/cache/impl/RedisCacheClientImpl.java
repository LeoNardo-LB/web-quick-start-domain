package org.smm.archetype.infrastructure.bizshared.client.cache.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure.bizshared.client.cache.AbstractCacheClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis缓存实现，基于Fastjson2序列化，适用于分布式场景。
 * @author Leonardo
 * @since 2026-01-10
 */
@Slf4j
public class RedisCacheClientImpl extends AbstractCacheClient {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheClientImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doGet(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // 由于使用了WriteClassName特性，Fastjson2会自动反序列化为正确的类型
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> List<T> doGetList(String key) {
        List<Object> range = redisTemplate.opsForList().range(key, 0, -1);
        if (range == null || range.isEmpty()) {
            return List.of();
        }
        // 直接转换，Fastjson2已经处理了类型信息，零拷贝高性能
        return range.stream()
                       .map(item -> (T) item)
                       .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> List<T> doGetList(String key, int beginIdx, int endIdx) {
        List<Object> range = redisTemplate.opsForList().range(key, beginIdx, endIdx);
        if (range == null || range.isEmpty()) {
            return List.of();
        }
        // 直接转换，Fastjson2已经处理了类型信息，零拷贝高性能
        return range.stream()
                       .map(item -> (T) item)
                       .collect(Collectors.toList());
    }

    @Override
    protected void doPut(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    protected void doPut(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    @Override
    protected void doAppend(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    protected void doDelete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    protected Boolean doHasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    protected Boolean doExpire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    @Override
    protected Long doGetExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

}
