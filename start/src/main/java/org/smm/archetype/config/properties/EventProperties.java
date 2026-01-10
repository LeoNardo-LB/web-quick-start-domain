package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 事件配置属性
 *
 * <p>支持 Spring Events 本地事件和 Kafka 消息队列两种实现。
 * @author Leonardo
 * @since 2026/1/10
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.event")
public class EventProperties {

    /**
     * 事件发布器配置
     */
    private Publisher publisher = new Publisher();

    /**
     * 事件重试配置
     */
    private Retry retry = new Retry();

    /**
     * 事件发布器配置
     */
    @Getter
    @Setter
    public static class Publisher {

        /**
         * 发布器类型：spring | kafka
         */
        private String type = "spring";

        /**
         * Kafka 配置
         */
        private Kafka kafka = new Kafka();

        /**
         * Spring Events 配置
         */
        private Spring spring = new Spring();

    }

    /**
     * Kafka 配置
     */
    @Getter
    @Setter
    public static class Kafka {

        /**
         * 主题前缀
         */
        private String topicPrefix = "domain-events-";

        /**
         * 是否启用发布确认
         */
        private Boolean enableAcks = true;

        /**
         * 超时时间
         */
        private Duration timeout = Duration.ofSeconds(5);

        /**
         * 重试次数
         */
        private Integer retries = 3;

    }

    /**
     * Spring Events 配置
     */
    @Getter
    @Setter
    public static class Spring {

        /**
         * 是否异步处理
         */
        private Boolean async = true;

        /**
         * 线程池核心大小
         */
        private Integer threadPoolCoreSize = 5;

        /**
         * 线程池最大大小
         */
        private Integer threadPoolMaxSize = 10;

        /**
         * 队列容量
         */
        private Integer threadPoolQueueCapacity = 100;

    }

    /**
     * 事件重试配置
     */
    @Getter
    @Setter
    public static class Retry {

        /**
         * Cron 表达式
         */
        private String cron = "0 * * * * ?";

        /**
         * 每批次处理数量
         */
        private Integer batchSize = 100;

        /**
         * 高优先级事件占比
         */
        private Double highPriorityRatio = 0.8;

    }

}
