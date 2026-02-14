package org.smm.archetype.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.smm.archetype.config.properties.ThreadPoolProperties;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

/**
 * 线程池配置类，配置IO密集型、CPU密集型和虚拟线程池。
 */
@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
@RequiredArgsConstructor
public class ThreadPoolConfigure implements AsyncConfigurer {

    /**
     * IO密集型线程池
     */
    public static final String IO_TASK_EXECUTOR  = "ioTaskExecutor";
    /**
     * CPU密集型线程池
     */
    public static final String CPU_TASK_EXECUTOR = "cpuTaskExecutor";

    /**
     * 虚拟线程池
     */
    public static final String VIRTUAL_TASK_EXECUTOR = "virtualTaskExecutor";

    /**
     * 守护线程池
     */
    public static final String DAEMON_TASK_EXECUTOR = "lowPriorityTaskExecutor";

    /**
     * MDC任务装饰器
     */
    private static final MdcTaskDecorator TASK_DECORATOR = new MdcTaskDecorator();

    /**
     * CPU核心数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 线程池属性
     */
    private final ThreadPoolProperties threadPoolProperties;

    /**
     * 创建 IO 密集型线程池（主线程池）
     * @return IO 密集型线程池
     */
    @Primary
    @Bean(name = IO_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 使用配置或默认值（保持动态计算）
        int coreSize = threadPoolProperties.getIo().getCoreSize() != null
                               ? threadPoolProperties.getIo().getCoreSize()
                               : CPU_COUNT;
        int maxSize = threadPoolProperties.getIo().getMaxSize() != null
                              ? threadPoolProperties.getIo().getMaxSize()
                              : CPU_COUNT * 2;

        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(threadPoolProperties.getIo().getQueueCapacity());
        executor.setThreadNamePrefix(threadPoolProperties.getIo().getThreadNamePrefix());

        // 设置拒绝策略
        String rejectionPolicy = threadPoolProperties.getIo().getRejectionPolicy();
        executor.setRejectedExecutionHandler(
                switch (rejectionPolicy) {
                    case "AbortPolicy" -> new AbortPolicy();
                    case "DiscardPolicy" -> new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy();
                    case "DiscardOldestPolicy" -> new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy();
                    default -> new CallerRunsPolicy();
                }
        );

        executor.setTaskDecorator(TASK_DECORATOR);
        executor.initialize();
        return executor;
    }

    /**
     * 创建 CPU 密集型线程池
     * @return CPU 密集型线程池
     */
    @Bean(name = CPU_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor cpuTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 使用配置或默认值（保持动态计算）
        int coreSize = threadPoolProperties.getCpu().getCoreSize() != null
                               ? threadPoolProperties.getCpu().getCoreSize()
                               : CPU_COUNT;
        int maxSize = threadPoolProperties.getCpu().getMaxSize() != null
                              ? threadPoolProperties.getCpu().getMaxSize()
                              : CPU_COUNT + 1;

        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(threadPoolProperties.getCpu().getQueueCapacity());
        executor.setThreadNamePrefix(threadPoolProperties.getCpu().getThreadNamePrefix());

        // 设置拒绝策略
        String rejectionPolicy = threadPoolProperties.getCpu().getRejectionPolicy();
        executor.setRejectedExecutionHandler(
                switch (rejectionPolicy) {
                    case "CallerRunsPolicy" -> new CallerRunsPolicy();
                    case "DiscardPolicy" -> new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy();
                    case "DiscardOldestPolicy" -> new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy();
                    default -> new AbortPolicy();
                }
        );

        executor.setTaskDecorator(TASK_DECORATOR);
        executor.initialize();
        return executor;
    }

    /**
     * 创建守护线程池
     * @return 守护线程池
     */
    @Bean(name = DAEMON_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor daemonTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 使用配置或默认值（保持动态计算）
        int coreSize = threadPoolProperties.getDaemon().getCoreSize() != null
                               ? threadPoolProperties.getDaemon().getCoreSize()
                               : CPU_COUNT / 2;
        int maxSize = threadPoolProperties.getDaemon().getMaxSize() != null
                              ? threadPoolProperties.getDaemon().getMaxSize()
                              : CPU_COUNT;

        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(threadPoolProperties.getDaemon().getQueueCapacity());
        executor.setThreadNamePrefix(threadPoolProperties.getDaemon().getThreadNamePrefix());
        executor.setTaskDecorator(TASK_DECORATOR);
        executor.setThreadPriority(threadPoolProperties.getDaemon().getThreadPriority());
        executor.initialize();
        return executor;
    }

    @Bean(name = VIRTUAL_TASK_EXECUTOR)
    public VirtualThreadTaskExecutor virtualTaskExecutor() {
        return new EnhanceVirtualThreadTaskExecutor("virtual-task-", TASK_DECORATOR);
    }

    /**
     * 虚拟线程池（ExecutorService类型）
     *
    用于需要ExecutorService类型的场景，如EventRetrySchedulerImpl。
     * @return ExecutorService
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 创建任务调度器
     * @return 任务调度器
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 使用配置或默认值（保持动态计算）
        int poolSize = threadPoolProperties.getScheduler().getPoolSize() != null
                               ? threadPoolProperties.getScheduler().getPoolSize()
                               : CPU_COUNT;

        scheduler.setPoolSize(poolSize);
        // 设置线程名称前缀，便于识别和调试
        scheduler.setThreadNamePrefix(threadPoolProperties.getScheduler().getThreadNamePrefix());
        // 设置是否在取消任务时从队列中移除
        scheduler.setRemoveOnCancelPolicy(threadPoolProperties.getScheduler().getRemoveOnCancelPolicy());
        // 设置在关闭调度器后是否继续执行已存在的周期性任务
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        // 设置在关闭调度器后是否继续执行已存在的任务
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        // 设置等待任务完成的超时时间（秒）
        scheduler.setAwaitTerminationSeconds(threadPoolProperties.getScheduler().getAwaitTerminationSeconds());
        scheduler.setTaskDecorator(TASK_DECORATOR);
        return scheduler;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return AsyncConfigurer.super.getAsyncUncaughtExceptionHandler();
    }

    /**
     * 虚拟线程
     */
    private static class EnhanceVirtualThreadTaskExecutor extends VirtualThreadTaskExecutor {

        private final TaskDecorator taskDecorator;

        public EnhanceVirtualThreadTaskExecutor(String threadNamePrefix, TaskDecorator taskDecorator) {
            super(threadNamePrefix);
            this.taskDecorator = taskDecorator;
        }

        public EnhanceVirtualThreadTaskExecutor(String threadNamePrefix) {
            super(threadNamePrefix);
            taskDecorator = task -> task;
        }

        @Override
        public void execute(@NonNull Runnable task) {
            Runnable decorated = taskDecorator.decorate(task);
            super.execute(decorated);
        }

        @Override
        @NonNull
        public Future<?> submit(@NonNull Runnable task) {
            Runnable decorated = taskDecorator.decorate(task);
            return super.submit(decorated);
        }

        @Override
        @NonNull
        public <T> Future<T> submit(@NonNull Callable<T> task) {
            FutureTask<T> futureTask = new FutureTask<>(task);
            Runnable decorated = taskDecorator.decorate(futureTask);
            super.execute(decorated);
            return futureTask;
        }

        @Override
        @NonNull
        public CompletableFuture<Void> submitCompletable(@NonNull Runnable task) {
            Runnable decorate = taskDecorator.decorate(task);
            return super.submitCompletable(decorate);
        }

        @Override
        @NonNull
        public <T> CompletableFuture<T> submitCompletable(@NonNull Callable<T> task) {
            CompletableFuture<T> future = new CompletableFuture<>();
            Runnable decorated = taskDecorator.decorate(() -> {
                try {
                    T result = task.call();
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            super.execute(decorated);
            return future;
        }

    }

    /**
     * MDC任务装饰器
     */
    private static class MdcTaskDecorator implements TaskDecorator {

        @Override
        @org.springframework.lang.NonNull
        public Runnable decorate(@org.springframework.lang.NonNull Runnable runnable) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        }

    }

}
