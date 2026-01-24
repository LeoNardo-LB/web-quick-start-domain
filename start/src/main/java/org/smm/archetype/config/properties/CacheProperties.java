package org.smm.archetype.config.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 缓存配置属性
 *
 * <p>支持本地 Caffeine 缓存和 Redis 分布式缓存两种实现。
 * Spring Boot 会根据依赖存在性自动选择正确的 Bean：
 * <ul>
 *   <li>Redis 依赖存在时：使用 {@link Redis} 配置，优先注册 {@link org.smm.archetype.infrastructure.common.cache.RedisCacheClientImpl}</li>
 *   <li>Redis 依赖不存在时：使用 {@link Local} 配置，注册 {@link org.smm.archetype.infrastructure.common.cache.CaffeineCacheClientImpl}</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026/1/10
 */
@Data
@ConfigurationProperties(prefix = "middleware.cache")
public class CacheProperties {

    /**
     * Redis 配置
     */
    private Redis redis = new Redis();

    /**
     * 本地缓存配置
     */
    private Local local = new Local();

    /**
     * Redis 配置
     */
    @Getter
    @Setter
    public static class Redis {

        /**
         * 缓存键前缀
         */
        private String keyPrefix = "app:";

        /**
         * 默认过期时间
         */
        private Duration defaultTtl = Duration.ofMinutes(30);

        /**
         * 是否缓存 null 值
         */
        private boolean cacheNullValues = true;

    }

    /**
     * 本地缓存配置
     */
    @Getter
    @Setter
    public static class Local {

        /**
         * 初始容量
         */
        private Integer initialCapacity = 1000;

        /**
         * 最大容量
         */
        private Long maximumSize = 10000L;

        /**
         * 写入后过期时间
         */
        private Duration expireAfterWrite = Duration.ofDays(30);

        /**
         * 访问后过期时间
         */
        private Duration expireAfterAccess = Duration.ofDays(30);

    }

}
