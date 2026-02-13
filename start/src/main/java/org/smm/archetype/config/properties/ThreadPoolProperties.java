package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池配置属性类。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.thread-pool")
public class ThreadPoolProperties {

    /**
     * IO密集型线程池配置
     * 用于执行IO密集型任务（文件读写、网络请求等）
     */
    private Io io = new Io();

    /**
     * CPU密集型线程池配置
     * 用于执行CPU密集型任务（计算、加密等）
     */
    private Cpu cpu = new Cpu();

    /**
     * 守护线程池配置
     * 用于执行低优先级后台任务
     */
    private Daemon daemon = new Daemon();

    /**
     * 任务调度线程池配置
     * 用于执行定时任务和延迟任务
     */
    private Scheduler scheduler = new Scheduler();

    /**
     * IO密集型线程池配置
     */
    @Getter
    @Setter
    public static class Io {
        /**
         * 核心线程数
         * 可选配置，默认使用CPU核心数（动态计算）
         */
        private Integer coreSize;

        /**
         * 最大线程数
         * 可选配置，默认使用CPU核心数 * 2（动态计算）
         */
        private Integer maxSize;

        /**
         * 队列容量
         * 默认：1000
         */
        private Integer queueCapacity = 1000;

        /**
         * 线程名称前缀
         * 默认：io-task-
         */
        private String threadNamePrefix = "io-task-";

        /**
         * 拒绝策略
         * 可选值：CallerRunsPolicy, AbortPolicy, DiscardPolicy, DiscardOldestPolicy
         * 默认：CallerRunsPolicy
         */
        private String rejectionPolicy = "CallerRunsPolicy";
    }

    /**
     * CPU密集型线程池配置
     */
    @Getter
    @Setter
    public static class Cpu {
        /**
         * 核心线程数
         * 可选配置，默认使用CPU核心数（动态计算）
         */
        private Integer coreSize;

        /**
         * 最大线程数
         * 可选配置，默认使用CPU核心数 + 1（动态计算）
         */
        private Integer maxSize;

        /**
         * 队列容量
         * 默认：100
         */
        private Integer queueCapacity = 100;

        /**
         * 线程名称前缀
         * 默认：cpu-task-
         */
        private String threadNamePrefix = "cpu-task-";

        /**
         * 拒绝策略
         * 可选值：CallerRunsPolicy, AbortPolicy, DiscardPolicy, DiscardOldestPolicy
         * 默认：AbortPolicy
         */
        private String rejectionPolicy = "AbortPolicy";
    }

    /**
     * 守护线程池配置
     */
    @Getter
    @Setter
    public static class Daemon {
        /**
         * 核心线程数
         * 可选配置，默认使用CPU核心数 / 2（动态计算）
         */
        private Integer coreSize;

        /**
         * 最大线程数
         * 可选配置，默认使用CPU核心数（动态计算）
         */
        private Integer maxSize;

        /**
         * 队列容量
         * 默认：100
         */
        private Integer queueCapacity = 100;

        /**
         * 线程名称前缀
         * 默认：daemon-task-
         */
        private String threadNamePrefix = "daemon-task-";

        /**
         * 线程优先级
         * 可选值：1-10，默认：1（MIN_PRIORITY）
         */
        private Integer threadPriority = 1;
    }

    /**
     * 任务调度线程池配置
     */
    @Getter
    @Setter
    public static class Scheduler {
        /**
         * 线程池大小
         * 可选配置，默认使用CPU核心数（动态计算）
         */
        private Integer poolSize;

        /**
         * 线程名称前缀
         * 默认：scheduled-task-
         */
        private String threadNamePrefix = "scheduled-task-";

        /**
         * 是否在取消任务时从队列中移除
         * 默认：true
         */
        private Boolean removeOnCancelPolicy = true;

        /**
         * 等待任务完成的超时时间（秒）
         * 默认：60
         */
        private Integer awaitTerminationSeconds = 60;
    }
}