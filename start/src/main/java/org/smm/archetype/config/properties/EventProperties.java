package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件配置属性类，支持Spring Events和Kafka。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.domain-event")
public class EventProperties {

    /**
     * 事件重试配置
     */
    private Retry retry = new Retry();

    /**
     * 事件消费者配置
     */
    private Consumer consumer = new Consumer();

    /**
     * 事件消费者配置
     */
    @Getter
    @Setter
    public static class Consumer {

        /**
         * Kafka 配置
         */
        private Kafka kafka = new Kafka();

        /**
         * Kafka 配置
         */
        @Getter
        @Setter
        public static class Kafka {

            /**
             * Kafka 主题
             */
            private String topic = "domain-events";

        }

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

        /**
         * 重试延迟时间列表（分钟）
         * <ul>
         *   <li>索引0：第1次重试延迟</li>
         *   <li>索引1：第2次重试延迟</li>
         *   <li>索引2：第3次重试延迟</li>
         *   <li>...</li>
         * </ul>
         *
         如果重试次数超过列表长度，使用最后一个值。
         默认：[1, 5, 15, 30, 60]，即1分钟、5分钟、15分钟、30分钟、60分钟
         */
        private List<Integer> delays = new ArrayList<>(List.of(1, 5, 15, 30, 60));

        /**
         * 最大重试次数
        默认：5
         */
        private Integer maxRetryTimes = 5;

    }

}
