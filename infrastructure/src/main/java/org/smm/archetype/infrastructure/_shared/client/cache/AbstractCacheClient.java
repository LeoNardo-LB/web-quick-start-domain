package org.smm.archetype.infrastructure._shared.client.cache;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.CacheClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 抽象缓存服务
 *
 * <p>定义缓存服务的通用流程模板，提供统一的异常处理和日志记录。
 *
 * <p>具体实现类需要实现所有 doXxx 抽象方法，提供具体的缓存操作。
 * @author Leonardo
 * @since 2026-01-10
 */
@Slf4j
public abstract class AbstractCacheClient implements CacheClient {

    @Override
    public <T> T get(String key) {
        try {
            return doGet(key);
        } catch (Exception e) {
            log.error("Cache get error, key: {}, implementation: {}", key, this.getClass().getSimpleName(), e);
            return null;
        }
    }

    @Override
    public <T> List<T> getList(String key) {
        try {
            return doGetList(key);
        } catch (Exception e) {
            log.error("Cache getList error, key: {}, implementation: {}", key, this.getClass().getSimpleName(), e);
            return List.of();
        }
    }

    @Override
    public <T> List<T> getList(String key, int beginIdx, int endIdx) {
        try {
            return doGetList(key, beginIdx, endIdx);
        } catch (Exception e) {
            log.error("Cache getList with range error, key: {}, range: [{}, {}], implementation: {}",
                    key, beginIdx, endIdx, this.getClass().getSimpleName(), e);
            return List.of();
        }
    }

    @Override
    public void put(String key, Object value) {
        try {
            doPut(key, value);
        } catch (Exception e) {
            log.error("Cache put error, key: {}, implementation: {}", key, this.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void put(String key, Object value, Duration duration) {
        try {
            doPut(key, value, duration);
        } catch (Exception e) {
            log.error("Cache put with duration error, key: {}, duration: {}, implementation: {}",
                    key, duration, this.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void append(String key, Object value) {
        try {
            doAppend(key, value);
        } catch (Exception e) {
            log.error("Cache append error, key: {}, implementation: {}", key, this.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            doDelete(key);
        } catch (Exception e) {
            log.error("Cache delete error, key: {}, implementation: {}", key, this.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Boolean hasKey(String key) {
        try {
            return doHasKey(key);
        } catch (Exception e) {
            log.error("Cache hasKey error, key: {}, implementation: {}", key, this.getClass().getSimpleName(), e);
            return false;
        }
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return doExpire(key, timeout, unit);
        } catch (Exception e) {
            log.error("Cache expire error, key: {}, timeout: {}, unit: {}, implementation: {}",
                    key, timeout, unit, this.getClass().getSimpleName(), e);
            return false;
        }
    }

    @Override
    public Long getExpire(String key) {
        try {
            return doGetExpire(key);
        } catch (Exception e) {
            log.error("Cache getExpire error, key: {}, implementation: {}", key, this.getClass().getSimpleName(), e);
            return -1L;
        }
    }

    // ========== 抽象扩展点 ==========

    /**
     * 获取缓存值
     * @param key 缓存键
     * @param <T> 返回值类型
     * @return 缓存值
     */
    protected abstract <T> T doGet(String key);

    /**
     * 获取列表
     * @param key 缓存键
     * @param <T> 列表元素类型
     * @return 列表值
     */
    protected abstract <T> List<T> doGetList(String key);

    /**
     * 获取列表（分页）
     * @param key      缓存键
     * @param beginIdx 开始索引
     * @param endIdx   结束索引
     * @param <T>      列表元素类型
     * @return 列表值
     */
    protected abstract <T> List<T> doGetList(String key, int beginIdx, int endIdx);

    /**
     * 存储缓存值
     * @param key   缓存键
     * @param value 缓存值
     */
    protected abstract void doPut(String key, Object value);

    /**
     * 存储缓存值（带过期时间）
     * @param key      缓存键
     * @param value    缓存值
     * @param duration 过期时间
     */
    protected abstract void doPut(String key, Object value, Duration duration);

    /**
     * 追加值到列表
     * @param key   缓存键
     * @param value 追加的值
     */
    protected abstract void doAppend(String key, Object value);

    /**
     * 删除缓存
     * @param key 缓存键
     */
    protected abstract void doDelete(String key);

    /**
     * 检查键是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    protected abstract Boolean doHasKey(String key);

    /**
     * 设置过期时间
     * @param key     缓存键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 是否成功
     */
    protected abstract Boolean doExpire(String key, long timeout, TimeUnit unit);

    /**
     * 获取过期时间
     * @param key 缓存键
     * @return 剩余过期时间（秒），-1表示不支持或永不过期
     */
    protected abstract Long doGetExpire(String key);

}
