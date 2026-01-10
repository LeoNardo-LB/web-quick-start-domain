package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Kafka消费者配置属性
 *
 * <p>配置Kafka消息队列的消费者参数。
 * @author Leonardo
 * @since 2026/1/10
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.kafka.consumer")
public class KafkaProperties {

    /**
     * Bootstrap服务器列表
     * <p>默认：localhost:9092
     */
    private String bootstrapServers = "localhost:9092";

    /**
     * 消费者组ID
     * <p>默认：kafka-consumer-group
     */
    private String groupId = "kafka-consumer-group";

    /**
     * 信任的包名列表
     * <p>用于JsonDeserializer反序列化，默认："*"（所有包）
     */
    private String trustedPackages = "*";

    /**
     * 订阅的主题列表
     * <p>默认：["domain-events"]
     */
    private List<String> topics = new ArrayList<>(List.of("domain-events"));

    /**
     * Key反序列化器
     * <p>默认：StringDeserializer
     */
    private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";

    /**
     * Value反序列化器
     * <p>默认：JsonDeserializer
     */
    private String valueDeserializer = "org.springframework.kafka.support.serializer.JsonDeserializer";

    /**
     * 自动提交偏移量
     * <p>默认：false（手动提交，确保事务一致性）
     */
    private Boolean enableAutoCommit = false;

    /**
     * 自动偏移量重置策略
     * <p>可选值：earliest, latest, none
     * <p>默认：earliest
     */
    private String autoOffsetReset = "earliest";

    /**
     * 最大轮询记录数
     * <p>默认：500
     */
    private Integer maxPollRecords = 500;

    /**
     * 最大轮询间隔（毫秒）
     * <p>默认：300000（5分钟）
     */
    private Long maxPollIntervalMs = 300000L;

}
