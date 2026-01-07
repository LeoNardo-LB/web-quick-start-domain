package org.smm.archetype.domain._shared.util;

import org.smm.archetype.domain._shared.service.CacheService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 * @author Leonardo
 * @since 2026/1/7
 */
public class CacheHolder {

    /**
     * 获取包装类
     * @param type         业务类型
     * @param cacheService 缓存服务
     * @return 包装类
     */
    public static BizCache of(Type type, CacheService cacheService) {
        return new BizCache(type, cacheService);
    }

    /**
     * 业务类型
     */
    public enum Type {

        ;

        /**
         * 构建缓存key
         * @param key 缓存 key
         * @return 缓存 key 具体的值
         */
        public String buildKey(String key) {
            return this.name() + ":" + key;
        }
    }

    /**
     * 业务缓存
     * @param type         业务类型
     * @param cacheService 缓存服务
     */
    public record BizCache(Type type, CacheService cacheService) implements CacheService {

        @Override
        public <T> T get(String key) {
            return cacheService.get(type.buildKey(key));
        }

        @Override
        public <T> List<T> getList(String key) {
            return cacheService.getList(type.buildKey(key));
        }

        @Override
        public <T> List<T> getList(String key, int beginIdx, int endIdx) {
            return cacheService.getList(type.buildKey(key), beginIdx, endIdx);
        }

        @Override
        public void put(String key, Object value) {
            cacheService.put(type.buildKey(key), value);
        }

        @Override
        public void put(String key, Object value, Duration duration) {
            cacheService.put(type.buildKey(key), value, duration);
        }

        @Override
        public void append(String key, Object value) {
            cacheService.append(type.buildKey(key), value);
        }

        @Override
        public void delete(String key) {
            cacheService.delete(type.buildKey(key));
        }

        @Override
        public Boolean hasKey(String key) {
            return cacheService.hasKey(type.buildKey(key));
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit unit) {
            return cacheService.expire(type.buildKey(key), timeout, unit);
        }

        @Override
        public Long getExpire(String key) {
            return cacheService.getExpire(type.buildKey(key));
        }

    }

}
