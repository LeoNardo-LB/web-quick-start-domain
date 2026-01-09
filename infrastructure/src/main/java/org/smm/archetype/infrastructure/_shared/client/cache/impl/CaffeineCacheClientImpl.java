package org.smm.archetype.infrastructure._shared.client.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure._shared.client.cache.AbstractCacheClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存服务实现
 *
 * <p>Caffeine 是基于 Java 8 的高性能本地缓存库，提供了：
 * <ul>
 *   <li>自动加载</li>
 *   <li>基于大小的驱逐</li>
 *   <li>基于时间的过期</li>
 *   <li>自动刷新</li>
 * </ul>
 *
 * <p>适用于：
 * <ul>
 *   <li>单机应用</li>
 *   <li>开发测试环境</li>
 *   <li>Redis的降级方案</li>
 * </ul>
 * @author Leonardo
 * @since 2026-01-10
 */
@Slf4j
public class CaffeineCacheClientImpl extends AbstractCacheClient {

    // 默认配置值
    private static final int DEFAULT_INITIAL_CAPACITY            = 100;
    private static final int DEFAULT_MAXIMUM_SIZE                = 1000;
    private static final int DEFAULT_EXPIRE_AFTER_WRITE_MINUTES  = 10;
    private static final int DEFAULT_EXPIRE_AFTER_ACCESS_MINUTES = 5;

    private final Cache<String, Object> cache;

    /**
     * 构造函数，使用默认配置初始化Caffeine缓存
     */
    public CaffeineCacheClientImpl() {
        this.cache = Caffeine.newBuilder()
                             .initialCapacity(DEFAULT_INITIAL_CAPACITY)
                             .maximumSize(DEFAULT_MAXIMUM_SIZE)
                             .expireAfterWrite(DEFAULT_EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                             .expireAfterAccess(DEFAULT_EXPIRE_AFTER_ACCESS_MINUTES, TimeUnit.MINUTES)
                             .build();

        log.info("Caffeine cache initialized with default config: initialCapacity={}, maximumSize={}, " +
                         "expireAfterWrite={}min, expireAfterAccess={}min",
                DEFAULT_INITIAL_CAPACITY, DEFAULT_MAXIMUM_SIZE,
                DEFAULT_EXPIRE_AFTER_WRITE_MINUTES, DEFAULT_EXPIRE_AFTER_ACCESS_MINUTES);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doGet(String key) {
        return (T) cache.getIfPresent(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> List<T> doGetList(String key) {
        Object value = cache.getIfPresent(key);
        if (value instanceof List list) {
            return (List<T>) list;
        }
        return List.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> List<T> doGetList(String key, int beginIdx, int endIdx) {
        Object value = cache.getIfPresent(key);
        if (value instanceof List list) {
            int size = list.size();
            int fromIndex = Math.max(0, beginIdx);
            int toIndex = Math.min(size, endIdx);
            if (fromIndex >= toIndex) {
                return List.of();
            }
            return (List<T>) list.subList(fromIndex, toIndex);
        }
        return List.of();
    }

    @Override
    protected void doPut(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    protected void doPut(String key, Object value, Duration duration) {
        cache.put(key, value);
        // Caffeine 不支持单独设置某个key的过期时间
        // 使用全局的expireAfterWrite配置
    }

    @Override
    protected void doAppend(String key, Object value) {
        // Caffeine 不支持 List 操作，使用 put 覆盖
        // 实际使用中建议使用 put 而不是 append
        cache.put(key, value);
    }

    @Override
    protected void doDelete(String key) {
        cache.invalidate(key);
    }

    @Override
    protected Boolean doHasKey(String key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    protected Boolean doExpire(String key, long timeout, TimeUnit unit) {
        // Caffeine 不支持动态设置单个key的过期时间
        // 使用全局的expireAfterWrite和expireAfterAccess配置
        return false;
    }

    @Override
    protected Long doGetExpire(String key) {
        // Caffeine 不支持获取剩余过期时间
        return -1L;
    }

}
