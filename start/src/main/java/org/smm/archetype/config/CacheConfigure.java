package org.smm.archetype.config;

import org.smm.archetype.config.properties.CacheProperties;
import org.smm.archetype.domain.bizshared.client.CacheClient;
import org.smm.archetype.infrastructure.bizshared.client.cache.impl.CaffeineCacheClientImpl;
import org.smm.archetype.infrastructure.bizshared.client.cache.impl.RedisCacheClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 缓存服务配置类，自动检测Redis并配置缓存客户端。
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfigure {

    private final CacheProperties properties;

    public CacheConfigure(CacheProperties properties) {
        this.properties = properties;
    }

    /**
     * 本地组件：Caffeine缓存服务（默认实现）
     *
     * <p>作为兜底方案，当RedisTemplate不存在时才创建此Bean。
     *
     * <p>使用@ConditionalOnMissingBean确保Redis优先级更高（@Primary）。
     * @return Caffeine缓存服务实现
     */
    @Bean
    public CacheClient caffeineCacheService() {
        return new CaffeineCacheClientImpl(
                properties.getInitialCapacity(),
                properties.getMaximumSize(),
                properties.getExpireAfterWrite()
        );
    }

    /**
     * 外部中间件：Redis缓存服务
     *
     * <p>条件：当RedisTemplate Bean存在时才创建此Bean。
     *
     * <p>使用@Primary标记为优先Bean，自动覆盖Caffeine缓存。
     * @param redisTemplate Redis模板（由Spring Data Redis自动配置）
     * @return Redis缓存服务实现
     */
    @Bean
    @Primary
    @ConditionalOnBooleanProperty("spring.data.redis")
    public CacheClient redisCacheService(RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheClientImpl(redisTemplate);
    }

}
