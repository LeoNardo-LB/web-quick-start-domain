package org.smm.archetype.domain.shared.util;

import org.smm.archetype.domain.shared.client.CacheClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类，提供类型化缓存key前缀。
 */
public class CacheHolder {

    /**
     * 获取包装类
     * @param type        业务类型
     * @param cacheClient 缓存服务
     * @return 包装类
     */
    public static BizCache of(Type type, CacheClient cacheClient) {
        return new BizCache(type, cacheClient);
    }

    /**
     * 业务类型
     */
    private enum Type {

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
     * @param type        业务类型
     * @param cacheClient 缓存服务
     */
    private record BizCache(Type type, CacheClient cacheClient) implements CacheClient {

        @Override
        public <T> T get(String key) {
            return cacheClient.get(type.buildKey(key));
        }

        @Override
        public <T> List<T> getList(String key) {
            return cacheClient.getList(type.buildKey(key));
        }

        @Override
        public <T> List<T> getList(String key, int beginIdx, int endIdx) {
            return cacheClient.getList(type.buildKey(key), beginIdx, endIdx);
        }

        @Override
        public void put(String key, Object value) {
            cacheClient.put(type.buildKey(key), value);
        }

        @Override
        public void put(String key, Object value, Duration duration) {
            cacheClient.put(type.buildKey(key), value, duration);
        }

        @Override
        public void append(String key, Object value) {
            cacheClient.append(type.buildKey(key), value);
        }

        @Override
        public void delete(String key) {
            cacheClient.delete(type.buildKey(key));
        }

        @Override
        public Boolean hasKey(String key) {
            return cacheClient.hasKey(type.buildKey(key));
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit unit) {
            return cacheClient.expire(type.buildKey(key), timeout, unit);
        }

        @Override
        public Long getExpire(String key) {
            return cacheClient.getExpire(type.buildKey(key));
        }

    }

}
