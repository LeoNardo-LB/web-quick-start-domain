package org.smm.archetype.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.smm.archetype.config.properties.CacheProperties;
import org.smm.archetype.domain._shared.client.CacheClient;
import org.smm.archetype.infrastructure._shared.client.cache.impl.CaffeineCacheClientImpl;
import org.smm.archetype.infrastructure._shared.client.cache.impl.RedisCacheClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Infrastructure层缓存服务配置
 *
 * <p>Bean装配策略：
 * <ul>
 *   <li>本地组件：Caffeine缓存作为默认实现（兜底方案），总是会被创建</li>
 *   <li>外部中间件：Redis缓存在RedisTemplate存在时自动覆盖Caffeine</li>
 *   <li>使用@ConditionalOnBean检测外部中间件，使用@Primary标记优先级</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>启动时确定：所有中间件在应用启动时通过Bean装配确定</li>
 *   <li>无运行时切换：不支持运行时动态切换中间件</li>
 *   <li>本地兜底：即使没有外部中间件，应用也能正常运行</li>
 * </ul>
 * @author Leonardo
 * @since 2026-01-10
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
     * <p>作为兜底方案，总是会被创建。
     *
     * <p>只有在没有RedisTemplate时才会作为主Bean使用。
     * @return Caffeine缓存服务实现
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.cache",
            name = "type",
            havingValue = "local",
            matchIfMissing = true
    )
    public CacheClient caffeineCacheService() {
        CacheProperties.Local local = properties.getLocal();
        return new CaffeineCacheClientImpl(
                local.getInitialCapacity(),
                local.getMaximumSize(),
                local.getExpireAfterWrite()
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
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnProperty(
            prefix = "middleware.cache",
            name = "type",
            havingValue = "redis"
    )
    public CacheClient redisCacheService(final RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheClientImpl(redisTemplate);
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Fastjson2序列化器
        FastJson2RedisSerializer<Object> fastJsonSerializer = new FastJson2RedisSerializer<>();

        // 设置key和value的序列化器
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(fastJsonSerializer);
        template.setHashValueSerializer(fastJsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 自定义Fastjson2序列化器，充分利用Fastjson2的加速特性
     */
    static class FastJson2RedisSerializer<T> implements RedisSerializer<T> {

        @Override
        public byte[] serialize(T t) throws SerializationException {
            if (t == null) {
                return new byte[0];
            }
            try {
                // 使用Fastjson2的高性能序列化特性
                // WriteClassName: 写入类型信息，反序列化时自动恢复类型
                // ReferenceDetection: 引用检测，避免循环引用
                // WriteNulls: 写入null值
                return JSON.toJSONBytes(t,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.ReferenceDetection,
                        JSONWriter.Feature.WriteNulls);
            } catch (Exception e) {
                throw new SerializationException("Could not serialize: " + e.getMessage(), e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public T deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                // 使用Fastjson2的高性能反序列化特性
                // 由于序列化时使用了WriteClassName，这里可以安全地反序列化带类型信息的数据
                // UseNativeObject: 使用原生对象，提升性能
                // FieldBased: 基于字段的反序列化，无需getter/setter
                return (T) JSON.parseObject(bytes,
                        Object.class,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased);
            } catch (Exception e) {
                throw new SerializationException("Could not deserialize: " + e.getMessage(), e);
            }
        }

    }

}
