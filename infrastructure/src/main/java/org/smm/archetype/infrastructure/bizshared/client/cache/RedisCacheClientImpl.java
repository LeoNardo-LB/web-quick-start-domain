package org.smm.archetype.infrastructure.bizshared.client.cache;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.CacheClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis缓存实现，基于Fastjson2序列化，适用于分布式场景。
 */
@Slf4j
public class RedisCacheClientImpl implements CacheClient {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheClientImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("Redis缓存初始化成功（分布式缓存）: RedisTemplate={}", redisTemplate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // 由于使用了WriteClassName特性，Fastjson2会自动反序列化为正确的类型
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
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
    public <T> List<T> getList(String key, int beginIdx, int endIdx) {
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
    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void put(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    @Override
    public void append(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

}
