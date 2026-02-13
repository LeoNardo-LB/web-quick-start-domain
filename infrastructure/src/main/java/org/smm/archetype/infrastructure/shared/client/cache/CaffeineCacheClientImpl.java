package org.smm.archetype.infrastructure.shared.client.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.client.CacheClient;
import org.smm.archetype.infrastructure.shared.client.cache.AbstractCacheClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存实现，支持自定义过期和访问追踪。
 */
@Slf4j
public class CaffeineCacheClientImpl extends AbstractCacheClient {

    private final Cache<String, CacheValueWrapper> cache;

    private final Duration defaultExpireAfterWrite;

    /**
     * 初始化Caffeine缓存实例。
     * @param initialCapacity 初始容量
     * @param maximumSize 最大容量
     * @param expireAfterWrite 写入后过期时间
     */
    public CaffeineCacheClientImpl(Integer initialCapacity, Long maximumSize, Duration expireAfterWrite) {
        this.defaultExpireAfterWrite = expireAfterWrite;
        CaffeineExpiry expiry = new CaffeineExpiry();

        this.cache = Caffeine.newBuilder()
                             .initialCapacity(initialCapacity)
                             .maximumSize(maximumSize)
                             .expireAfter(expiry)
                             .build();

        log.info("Caffeine缓存初始化成功（自定义过期时间）: 初始容量={}, 最大大小={}, 默认写入过期时间={}",
                initialCapacity, maximumSize, expireAfterWrite);
    }

    // ==================== 扩展点实现（protected do* 方法） ====================

    @Override
    protected <T> T doGet(String key) throws Exception {
        CacheValueWrapper wrapper = cache.getIfPresent(key);
        if (wrapper == null) {
            return null;
        }

        // 更新访问时间
        wrapper.updateAccessTime();

        // 返回原始值
        return (T) wrapper.value();
    }

    @Override
    protected <T> List<T> doGetList(String key) throws Exception {
        CacheValueWrapper wrapper = cache.getIfPresent(key);
        if (wrapper == null) {
            return List.of();
        }

        wrapper.updateAccessTime();

        Object value = wrapper.value();
        if (value instanceof List<?> list) {
            return (List<T>) list;
        }
        return List.of();
    }

    @Override
    protected <T> List<T> doGetList(String key, int beginIdx, int endIdx) throws Exception {
        CacheValueWrapper wrapper = cache.getIfPresent(key);
        if (wrapper == null) {
            return List.of();
        }

        wrapper.updateAccessTime();

        Object value = wrapper.value();
        if (value instanceof List<?> list) {
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
    protected void doPut(String key, Object value) throws Exception {
        // 使用默认过期时间（从配置读取）
        CacheValueWrapper wrapper = CacheValueWrapper.of(value, defaultExpireAfterWrite);
        cache.put(key, wrapper);
    }

    @Override
    protected void doPut(String key, Object value, Duration duration) throws Exception {
        // 使用指定的过期时间
        CacheValueWrapper wrapper = CacheValueWrapper.of(value, duration);
        cache.put(key, wrapper);
    }

    @Override
    protected void doAppend(String key, Object value) throws Exception {
        // Caffeine 不支持 List 操作，使用 put 覆盖
        // 实际使用中建议使用 put 而不是 append
        CacheValueWrapper wrapper = CacheValueWrapper.of(value, defaultExpireAfterWrite);
        cache.put(key, wrapper);
    }

    @Override
    protected void doDelete(String key) throws Exception {
        cache.invalidate(key);
    }

    @Override
    protected Boolean doHasKey(String key) throws Exception {
        CacheValueWrapper wrapper = cache.getIfPresent(key);
        return wrapper != null;
    }

    @Override
    protected Boolean doExpire(String key, long timeout, TimeUnit unit) throws Exception {
        CacheValueWrapper wrapper = cache.getIfPresent(key);
        if (wrapper == null) {
            return false;
        }

        // 创建新的 Wrapper，更新过期时间
        Duration duration = Duration.ofMillis(unit.toMillis(timeout));
        CacheValueWrapper newWrapper = CacheValueWrapper.of(
                wrapper.value(),
                duration
        );
        // 注意：CacheValueWrapper 的 createTime 是 final，无法修改
        // 如果需要保持原创建时间，需要在 CacheValueWrapper 中添加相应的工厂方法

        cache.put(key, newWrapper);
        return true;
    }

    @Override
    protected Long doGetExpire(String key) throws Exception {
        CacheValueWrapper wrapper = cache.getIfPresent(key);
        if (wrapper == null) {
            return -1L;
        }

        if (wrapper.getExpireTime() == 0) {
            return -1L; // 永不过期
        }

        // 返回剩余秒数
        return wrapper.getRemainingTimeMillis() / 1000;
    }

    /**
     * Caffeine 自定义过期策略（私有内部类）
     *
    基于 {@link CacheValueWrapper} 的过期时间实现每个 Entry 的独立过期控制。
     *
    线程安全性：
     * <ul>
     *   <li>Caffeine 内部使用同步锁，同一时刻只有一个线程能操作特定 key</li>
     *   <li>此类的所有方法都在 Caffeine 的同步保护下调用</li>
     *   <li>Wrapper 的 volatile 字段保证了多线程间的可见性</li>
     * </ul>
     *
    设置为私有内部类的原因：
     * <ul>
     *   <li>防止外部引用，保持高内聚</li>
     *   <li>仅服务于 CaffeineCacheClientImpl</li>
     *   <li>避免被其他类误用</li>
     * </ul>
    
    
     */
    private static class CaffeineExpiry implements Expiry<String, CacheValueWrapper> {

        /**
         * 缓存条目创建时调用，返回过期时间
         *
         此方法在 Caffeine 的同步保护下调用，保证线程安全。
         * @param key         缓存键
         * @param wrapper     缓存值包装器
         * @param currentTime Caffeine 内部当前时间（纳秒）
         * @return 过期时间的纳秒数，Long.MAX_VALUE 表示永不过期
         */
        @Override
        public long expireAfterCreate(String key, CacheValueWrapper wrapper, long currentTime) {
            return wrapper.getExpireTimeNanos();
        }

        /**
         * 缓存条目更新时调用，返回新的过期时间
         *
         此方法在 Caffeine 的同步保护下调用，保证线程安全。
         * 当通过 put(key, value) 更新缓存时，会重新计算过期时间。
         * @param key         缓存键
         * @param wrapper     新的缓存值包装器
         * @param currentTime     Caffeine 内部当前时间（纳秒）
         * @return 新的过期时间纳秒数
         */
        @Override
        public long expireAfterUpdate(String key, CacheValueWrapper wrapper,
                                      long currentTime, long currentDuration) {
            return wrapper.getExpireTimeNanos();
        }

        /**
         * Entry 读取时调用，返回过期时间
         *
         此方法在 Caffeine 的同步保护下调用，保证线程安全。
         *
         注意：我们在此更新访问时间，但保持原过期时间不变（返回 currentDuration）。
         * 这样实现了基于访问时间追踪，但不延长过期时间的效果。
         * @param key             缓存键
         * @param wrapper         缓存值包装器
         * @param currentTime     Caffeine 内部当前时间（纳秒）
         * @param currentDuration 当前的剩余时间（纳秒）
         * @return 过期时间纳秒数（保持不变）
         */
        @Override
        public long expireAfterRead(String key, CacheValueWrapper wrapper,
                                    long currentTime, long currentDuration) {
            // 更新访问时间（volatile 写，保证可见性）
            wrapper.updateAccessTime();
            return currentDuration; // 保持不变
        }

    }

    /**
     * 缓存值包装器（线程安全）
     *
    封装原始值并添加时间属性，支持：
     * <ul>
     *   <li>存入时间追踪</li>
     *   <li>自定义过期时间</li>
     *   <li>访问时间记录</li>
     *   <li>Caffeine Expiry 接口集成</li>
     * </ul>
     *
    线程安全性说明：
     * <ul>
     *   <li>✓ 不可变字段（value, createTime）使用 final 保证初始化安全</li>
     *   <li>✓ 可变字段（expireTime, accessTime）使用 volatile 保证可见性</li>
     *   <li>✓ 依赖 Caffeine 内部同步机制保证操作原子性</li>
     *   <li>✓ 适用于高并发读多写少场景</li>
     * </ul>
     *
    ⚠️ 重要：不要在外部持有此对象的引用！
    此类的线程安全依赖于 Caffeine 的内部同步机制（类似 ConcurrentHashMap 的分段锁）。
     * 如果在缓存外部持有此对象并调用其方法，可能导致并发问题。
     *
    正确用法：
     * <pre>{@code
     * // 通过 CacheClient 访问，Wrapper 由 Caffeine 管理
     * String value = cacheClient.get("key");
     * }</pre>
     *
    错误用法：
     * <pre>{@code
     * // ✗ 禁止在外部持有 Wrapper 引用
     * CacheValueWrapper wrapper = ...;  // 不要这样做
     * wrapper.updateAccessTime();       // 可能导致并发问题
     * }</pre>
    
    
     */
    @Getter
    private static class CacheValueWrapper {

        /**
         * 原始值（不可变）
         */
        private final Object value;

        /**
         * 获取原始值（对外暴露的方法）
         */
        public Object value() {
            return this.value;
        }

        /**
         * 存入时间戳（毫秒，不可变）
         */
        private final long createTime;

        /**
         * 过期时间戳（毫秒，volatile 保证可见性）
         *
        使用 volatile 的原因：
         * <ul>
         *   <li>保证多线程间的可见性（happens-before 关系）</li>
         *   <li>防止指令重排序</li>
         *   <li>避免读取到过期时间的陈旧值</li>
         * </ul>
         *
        虽然此字段可能被 {@link #updateExpireTime(long)} 更新，
         * 但更新操作仅在 Caffeine 的同步保护下进行（如 doExpire 方法）。
         */
        private volatile long expireTime;

        /**
         * 获取过期时间戳
         * @return 过期时间戳（毫秒）
         */
        public long getExpireTime() {
            return this.expireTime;
        }

        /**
         * 最后访问时间戳（毫秒，volatile 保证可见性）
         *
        使用 volatile 的原因：
         * <ul>
         *   <li>保证多线程间的可见性</li>
         *   <li>虽然 {@link #updateAccessTime()} 在 Caffeine 同步保护下调用，
         *       但 volatile 确保了外部读取时的可见性</li>
         * </ul>
         */
        private volatile long accessTime;

        /**
         * 获取创建时间戳
         * @return 创建时间戳（毫秒）
         */
        public long getCreateTime() {
            return this.createTime;
        }

        /**
         * 获取访问时间戳
         * @return 访问时间戳（毫秒）
         */
        public long getAccessTime() {
            return this.accessTime;
        }

        /**
         * 私有构造函数
         */
        private CacheValueWrapper(Object value, long createTime, long expireTime, long accessTime) {
            this.value = value;
            this.createTime = createTime;
            this.expireTime = expireTime;
            this.accessTime = accessTime;
        }

        /**
         * 创建永久有效的 Wrapper
         *
         expireTime 设置为 0 表示永不过期。
         * @param value 原始值
         * @return Wrapper 实例
         */
        public static CacheValueWrapper of(Object value) {
            long now = System.currentTimeMillis();
            return new CacheValueWrapper(value, now, 0L, now);
        }

        /**
         * 创建带过期时间的 Wrapper
         * @param value    原始值
         * @param duration 过期时长
         * @return Wrapper 实例
         */
        public static CacheValueWrapper of(Object value, Duration duration) {
            long now = System.currentTimeMillis();
            long expireTime = now + duration.toMillis();
            return new CacheValueWrapper(value, now, expireTime, now);
        }

        /**
         * 是否已过期
         *
        此方法读取 volatile 字段，保证获取最新的过期时间。
         * @return true 如果已过期，false 如果未过期或永不过期
         */
        public boolean isExpired() {
            if (expireTime == 0) {
                return false; // 永不过期
            }
            return System.currentTimeMillis() >= expireTime;
        }

        /**
         * 获取剩余有效时间（毫秒）
         *
        此方法读取 volatile 字段，保证获取最新的过期时间。
         * @return 剩余毫秒数，-1 表示永不过期，0 表示已过期
         */
        public long getRemainingTimeMillis() {
            if (expireTime == 0) {
                return -1L; // 永不过期
            }
            long remaining = expireTime - System.currentTimeMillis();
            return Math.max(0L, remaining);
        }

        /**
         * 获取过期时间的纳秒数（用于 Caffeine Expiry）
         *
        此方法读取 volatile 字段并转换为纳秒，供 Caffeine 的 Expiry 接口使用。
         * @return 过期时间的纳秒数，Long.MAX_VALUE 表示永不过期
         */
        public long getExpireTimeNanos() {
            if (expireTime == 0) {
                return Long.MAX_VALUE; // 永不过期
            }
            long remainingMillis = expireTime - System.currentTimeMillis();
            return Math.max(0L, remainingMillis * 1_000_000); // 转换为纳秒
        }

        /**
         * 更新访问时间
         *
        ⚠️ 此方法应在 Caffeine 的同步保护下调用（如 expireAfterRead）。
         * 如果在外部调用，可能导致并发问题。
         *
        volatile 写操作保证了 happens-before 关系，确保其他线程能立即看到更新。
         */
        public void updateAccessTime() {
            this.accessTime = System.currentTimeMillis();
        }

        /**
         * 更新过期时间
         *
        ⚠️ 此方法应在 Caffeine 的同步保护下调用（如 doExpire）。
         * 如果在外部调用，可能导致并发问题。
         *
        volatile 写操作保证了 happens-before 关系，确保其他线程能立即看到更新。
         * @param expireTime 新的过期时间戳（毫秒）
         */
        public void updateExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

    }

}
