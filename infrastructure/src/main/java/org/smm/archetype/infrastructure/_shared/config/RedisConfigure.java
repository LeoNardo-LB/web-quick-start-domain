package org.smm.archetype.infrastructure._shared.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 使用Fastjson2序列化，启用加速特性
 *
 * @author Leonardo
 * @since 2026/1/7
 */
@Configuration
public class RedisConfigure {

    @Bean
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
