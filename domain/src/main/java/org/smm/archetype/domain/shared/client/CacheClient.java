package org.smm.archetype.domain.shared.client;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务


 */
public interface CacheClient {

    <T> T get(String key);

    <T> List<T> getList(String key);

    <T> List<T> getList(String key, int beginIdx, int endIdx);

    void put(String key, Object value);

    void put(String key, Object value, Duration duration);

    void append(String key, Object value);

    void delete(String key);

    Boolean hasKey(String key);

    Boolean expire(String key, long timeout, TimeUnit unit);

    Long getExpire(String key);

}
