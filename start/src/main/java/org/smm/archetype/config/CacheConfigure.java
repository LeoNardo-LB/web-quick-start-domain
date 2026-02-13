package org.smm.archetype.config;

import org.smm.archetype.config.properties.CacheProperties;
import org.smm.archetype.domain.shared.client.CacheClient;
import org.smm.archetype.infrastructure.shared.client.cache.CaffeineCacheClientImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
     * 作为兜底方案，当RedisTemplate不存在时才创建此Bean。
     * 使用@ConditionalOnMissingBean确保Redis优先级更高（@Primary）。
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

    // /**
    //  * 外部中间件：Redis缓存服务
    //  * @param redisTemplate Redis模板（由Spring Data Redis自动配置）
    //  * @return Redis缓存服务实现
    //  */
    // @Bean
    // @Primary
    // @ConditionalOnBooleanProperty("spring.data.redis")
    // public CacheClient redisCacheService(RedisTemplate<String, Object> redisTemplate) {
    //     return new RedisCacheClientImpl(redisTemplate);
    // }

}
