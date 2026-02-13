package org.smm.archetype.infrastructure.shared.client.cache;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.client.CacheClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务抽象基类，提供通用缓存操作模板。
 *
 * <p>基于Template Method模式，统一缓存操作的流程：
 * <ul>
 *   <li>参数验证</li>
 *   <li>统一日志记录</li>
 *   <li>异常处理</li>
 *   <li>调用子类实现的具体逻辑</li>
 * </ul>
 *
 * <p>扩展点：
 * <ul>
 *   <li>{@link #doGet(String)} - 获取缓存值</li>
 *   <li>{@link #doGetList(String)} - 获取列表缓存</li>
 *   <li>{@link #doGetList(String, int, int)} - 获取列表缓存（范围）</li>
 *   <li>{@link #doPut(String, Object)} - 设置缓存</li>
 *   <li>{@link #doPut(String, Object, Duration)} - 设置缓存（带过期时间）</li>
 *   <li>{@link #doAppend(String, Object)} - 追加到列表</li>
 *   <li>{@link #doDelete(String)} - 删除缓存</li>
 *   <li>{@link #doHasKey(String)} - 检查键是否存在</li>
 *   <li>{@link #doExpire(String, long, TimeUnit)} - 设置过期时间</li>
 *   <li>{@link #doGetExpire(String)} - 获取过期时间</li>
 * </ul>
 */
@Slf4j
public abstract class AbstractCacheClient implements CacheClient {

    // ==================== CacheClient 接口实现（模板方法） ====================

    @Override
    public final <T> T get(String key) {
        log.debug("Getting cache: key={}", key);
        validateKey(key);

        try {
            T value = doGet(key);
            log.debug("Cache get result: key={}, found={}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Failed to get cache: key={}", key, e);
            throw wrapException("Failed to get cache", e);
        }
    }

    @Override
    public final <T> List<T> getList(String key) {
        log.debug("Getting cache list: key={}", key);
        validateKey(key);

        try {
            List<T> list = doGetList(key);
            log.debug("Cache list get result: key={}, size={}", key, list != null ? list.size() : 0);
            return list;
        } catch (Exception e) {
            log.error("Failed to get cache list: key={}", key, e);
            throw wrapException("Failed to get cache list", e);
        }
    }

    @Override
    public final <T> List<T> getList(String key, int beginIdx, int endIdx) {
        log.debug("Getting cache list range: key={}, beginIdx={}, endIdx={}", key, beginIdx, endIdx);
        validateKey(key);

        try {
            List<T> list = doGetList(key, beginIdx, endIdx);
            log.debug("Cache list range get result: key={}, size={}", key, list != null ? list.size() : 0);
            return list;
        } catch (Exception e) {
            log.error("Failed to get cache list range: key={}, beginIdx={}, endIdx={}", key, beginIdx, endIdx, e);
            throw wrapException("Failed to get cache list range", e);
        }
    }

    @Override
    public final void put(String key, Object value) {
        log.debug("Putting cache: key={}, value={}", key, value);
        validateKey(key);

        try {
            doPut(key, value);
            log.debug("Cache put success: key={}", key);
        } catch (Exception e) {
            log.error("Failed to put cache: key={}", key, e);
            throw wrapException("Failed to put cache", e);
        }
    }

    @Override
    public final void put(String key, Object value, Duration duration) {
        log.debug("Putting cache with TTL: key={}, value={}, duration={}", key, value, duration);
        validateKey(key);
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        try {
            doPut(key, value, duration);
            log.debug("Cache put with TTL success: key={}, duration={}", key, duration);
        } catch (Exception e) {
            log.error("Failed to put cache with TTL: key={}", key, e);
            throw wrapException("Failed to put cache with TTL", e);
        }
    }

    @Override
    public final void append(String key, Object value) {
        log.debug("Appending to cache list: key={}, value={}", key, value);
        validateKey(key);

        try {
            doAppend(key, value);
            log.debug("Cache append success: key={}", key);
        } catch (Exception e) {
            log.error("Failed to append to cache list: key={}", key, e);
            throw wrapException("Failed to append to cache list", e);
        }
    }

    @Override
    public final void delete(String key) {
        log.debug("Deleting cache: key={}", key);
        validateKey(key);

        try {
            doDelete(key);
            log.debug("Cache delete success: key={}", key);
        } catch (Exception e) {
            log.error("Failed to delete cache: key={}", key, e);
            throw wrapException("Failed to delete cache", e);
        }
    }

    @Override
    public final Boolean hasKey(String key) {
        log.debug("Checking cache key existence: key={}", key);
        validateKey(key);

        try {
            Boolean exists = doHasKey(key);
            log.debug("Cache key exists: key={}, exists={}", key, exists);
            return exists;
        } catch (Exception e) {
            log.error("Failed to check cache key existence: key={}", key, e);
            throw wrapException("Failed to check cache key existence", e);
        }
    }

    @Override
    public final Boolean expire(String key, long timeout, TimeUnit unit) {
        log.debug("Setting cache expiration: key={}, timeout={}, unit={}", key, timeout, unit);
        validateKey(key);
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }

        try {
            Boolean result = doExpire(key, timeout, unit);
            log.debug("Cache expire set result: key={}, success={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to set cache expiration: key={}", key, e);
            throw wrapException("Failed to set cache expiration", e);
        }
    }

    @Override
    public final Long getExpire(String key) {
        log.debug("Getting cache expiration: key={}", key);
        validateKey(key);

        try {
            Long expire = doGetExpire(key);
            log.debug("Cache expiration: key={}, expireSeconds={}", key, expire);
            return expire;
        } catch (Exception e) {
            log.error("Failed to get cache expiration: key={}", key, e);
            throw wrapException("Failed to get cache expiration", e);
        }
    }

    // ==================== 扩展点（由子类实现） ====================

    /**
     * 获取缓存值（扩展点）
     * @param key 缓存键
     * @return 缓存值，不存在返回null
     * @throws Exception 获取失败
     */
    protected abstract <T> T doGet(String key) throws Exception;

    /**
     * 获取列表缓存（扩展点）
     * @param key 缓存键
     * @return 列表值，不存在返回空列表
     * @throws Exception 获取失败
     */
    protected abstract <T> List<T> doGetList(String key) throws Exception;

    /**
     * 获取列表缓存（范围）（扩展点）
     * @param key      缓存键
     * @param beginIdx 开始索引
     * @param endIdx   结束索引
     * @return 列表值，不存在返回空列表
     * @throws Exception 获取失败
     */
    protected abstract <T> List<T> doGetList(String key, int beginIdx, int endIdx) throws Exception;

    /**
     * 设置缓存（扩展点）
     * @param key   缓存键
     * @param value 缓存值
     * @throws Exception 设置失败
     */
    protected abstract void doPut(String key, Object value) throws Exception;

    /**
     * 设置缓存（带过期时间）（扩展点）
     * @param key      缓存键
     * @param value    缓存值
     * @param duration 过期时长
     * @throws Exception 设置失败
     */
    protected abstract void doPut(String key, Object value, Duration duration) throws Exception;

    /**
     * 追加到列表（扩展点）
     * @param key   缓存键
     * @param value 追加值
     * @throws Exception 追加失败
     */
    protected abstract void doAppend(String key, Object value) throws Exception;

    /**
     * 删除缓存（扩展点）
     * @param key 缓存键
     * @throws Exception 删除失败
     */
    protected abstract void doDelete(String key) throws Exception;

    /**
     * 检查键是否存在（扩展点）
     * @param key 缓存键
     * @return true-存在，false-不存在
     * @throws Exception 检查失败
     */
    protected abstract Boolean doHasKey(String key) throws Exception;

    /**
     * 设置过期时间（扩展点）
     * @param key     缓存键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true-成功，false-失败
     * @throws Exception 设置失败
     */
    protected abstract Boolean doExpire(String key, long timeout, TimeUnit unit) throws Exception;

    /**
     * 获取过期时间（扩展点）
     * @param key 缓存键
     * @return 剩余秒数，-1表示永不过期，-2表示键不存在
     * @throws Exception 获取失败
     */
    protected abstract Long doGetExpire(String key) throws Exception;

    // ==================== 辅助方法 ====================

    /**
     * 参数验证：验证缓存键是否有效
     * @param key 缓存键
     * @throws IllegalArgumentException 当key为null或blank时抛出
     */
    protected void validateKey(String key) {
        if (key == null || key.isBlank()) {
            log.error("Cache key cannot be null or blank");
            throw new IllegalArgumentException("Cache key cannot be null or blank");
        }
    }

    /**
     * 包装异常为统一运行时异常
     * @param message 错误消息
     * @param cause   原始异常
     * @return 运行时异常
     */
    protected RuntimeException wrapException(String message, Exception cause) {
        return new RuntimeException(message, cause);
    }

}
