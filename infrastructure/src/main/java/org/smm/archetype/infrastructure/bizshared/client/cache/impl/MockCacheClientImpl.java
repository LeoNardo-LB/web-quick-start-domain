package org.smm.archetype.infrastructure.bizshared.client.cache.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.CacheClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock缓存客户端实现,模拟缓存操作。
 * 用于测试和开发环境,避免依赖Redis中间件。
 */
@Slf4j
public class MockCacheClientImpl implements CacheClient {

    /**
     * 缓存数据存储
     */
    private final Map<String, Object> cacheData = new ConcurrentHashMap<>();

    /**
     * 缓存过期时间存储
     */
    private final Map<String, Long> cacheTtl = new ConcurrentHashMap<>();

    /**
     * 过期时间戳计数器
     */
    private final AtomicLong expireTimeCounter = new AtomicLong(0);

    /**
     * 检查key是否已过期
     */
    private boolean isExpired(String key) {
        Long expireTime = cacheTtl.get(key);
        return expireTime != null && expireTime < System.currentTimeMillis();
    }

    @Override
    public <T> T get(String key) {
        log.debug("Mock缓存获取: key={}", key);
        if (isExpired(key)) {
            cacheData.remove(key);
            cacheTtl.remove(key);
            return null;
        }
        return (T) cacheData.get(key);
    }

    @Override
    public <T> List<T> getList(String key) {
        log.debug("Mock缓存获取列表: key={}", key);
        if (isExpired(key)) {
            cacheData.remove(key);
            cacheTtl.remove(key);
            return null;
        }
        Object value = cacheData.get(key);
        if (value instanceof List) {
            return (List<T>) value;
        }
        return null;
    }

    @Override
    public <T> List<T> getList(String key, int beginIdx, int endIdx) {
        log.debug("Mock缓存获取列表(范围): key={}, beginIdx={}, endIdx={}", key, beginIdx, endIdx);
        if (isExpired(key)) {
            cacheData.remove(key);
            cacheTtl.remove(key);
            return null;
        }
        Object value = cacheData.get(key);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            int from = Math.max(0, beginIdx);
            int to = Math.min(list.size(), endIdx);
            if (from >= to) {
                return new ArrayList<>();
            }
            return new ArrayList<>((List<T>) list.subList(from, to));
        }
        return null;
    }

    @Override
    public void put(String key, Object value) {
        log.debug("Mock缓存设置: key={}, value={}", key, value);
        cacheData.put(key, value);
        cacheTtl.remove(key);
    }

    @Override
    public void put(String key, Object value, Duration duration) {
        log.debug("Mock缓存设置(TTL): key={}, value={}, duration={}s", key, value, duration.getSeconds());
        cacheData.put(key, value);
        long expireTime = System.currentTimeMillis() + (duration.getSeconds() * 1000);
        cacheTtl.put(key, expireTime);
        expireTimeCounter.incrementAndGet();
    }

    @Override
    public void append(String key, Object value) {
        log.debug("Mock缓存追加: key={}, value={}", key, value);
        Object existingValue = cacheData.get(key);
        if (existingValue instanceof List) {
            List<Object> list = (List<Object>) existingValue;
            list.add(value);
        } else {
            List<Object> list = new ArrayList<>();
            if (existingValue != null) {
                list.add(existingValue);
            }
            list.add(value);
            cacheData.put(key, list);
        }
    }

    @Override
    public void delete(String key) {
        log.debug("Mock缓存删除: key={}", key);
        cacheData.remove(key);
        cacheTtl.remove(key);
    }

    @Override
    public Boolean hasKey(String key) {
        log.debug("Mock缓存检查: key={}", key);
        if (isExpired(key)) {
            cacheData.remove(key);
            cacheTtl.remove(key);
            return false;
        }
        return cacheData.containsKey(key);
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        log.debug("Mock缓存设置过期: key={}, timeout={}, unit={}", key, timeout, unit);
        if (!cacheData.containsKey(key)) {
            return false;
        }
        long expireTime = System.currentTimeMillis() + unit.toMillis(timeout);
        cacheTtl.put(key, expireTime);
        expireTimeCounter.incrementAndGet();
        return true;
    }

    @Override
    public Long getExpire(String key) {
        log.debug("Mock缓存获取剩余时间: key={}", key);
        Long expireTime = cacheTtl.get(key);
        if (expireTime == null) {
            return 0L;
        }
        long remaining = expireTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0L;
    }
}
