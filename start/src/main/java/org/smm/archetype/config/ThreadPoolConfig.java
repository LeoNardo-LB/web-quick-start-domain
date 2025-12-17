package org.smm.archetype.config;

import org.smm.archetype.MdcTaskDecorator;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/13
 */
@Configuration
public class ThreadPoolConfig implements AsyncConfigurer {

    /**
     * IO密集型线程池
     */
    public static final String IO_TASK_EXECUTOR      = "ioTaskExecutor";
    /**
     * 虚拟线程池
     */
    public static final String VIRTUAL_TASK_EXECUTOR = "virtualTaskExecutor";
    /**
     * CPU密集型线程池
     */
    public static final String CPU_TASK_EXECUTOR          = "cpuTaskExecutor";
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

    @Primary
    @Bean(name = IO_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_COUNT);
        executor.setMaxPoolSize(CPU_COUNT * 2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("io-task-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.setTaskDecorator(TASK_DECORATOR);
        executor.initialize();
        return executor;
    }

    @Bean(name = CPU_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor cpuTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_COUNT); // CPU核心数相关
        executor.setMaxPoolSize(CPU_COUNT + 1);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("cpu-task-");
        executor.setRejectedExecutionHandler(new AbortPolicy());
        executor.setTaskDecorator(TASK_DECORATOR);
        executor.initialize();
        return executor;
    }

    @Bean(name = DAEMON_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor daemonTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_COUNT / 2);
        executor.setMaxPoolSize(CPU_COUNT);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("daemon-task-");
        executor.setTaskDecorator(TASK_DECORATOR);
        executor.setThreadPriority(Thread.MIN_PRIORITY);
        executor.initialize();
        return executor;
    }

    @Bean(name = VIRTUAL_TASK_EXECUTOR)
    public VirtualThreadTaskExecutor virtualTaskExecutor() {
        return new EnhanceVirtualThreadTaskExecutor("virtual-task-", TASK_DECORATOR);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(CPU_COUNT);
        // 设置线程名称前缀，便于识别和调试
        scheduler.setThreadNamePrefix("scheduled-task-");
        // 设置是否在取消任务时从队列中移除
        scheduler.setRemoveOnCancelPolicy(true);
        // 设置在关闭调度器后是否继续执行已存在的周期性任务
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        // 设置在关闭调度器后是否继续执行已存在的任务
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        // 设置等待任务完成的超时时间（秒）
        scheduler.setAwaitTerminationSeconds(60);
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
        public void execute(Runnable task) {
            Runnable decorated = taskDecorator.decorate(task);
            super.execute(decorated);
        }

        @Override
        public Future<?> submit(Runnable task) {
            Runnable decorated = taskDecorator.decorate(task);
            return super.submit(decorated);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            // 将Callable包装为FutureTask（FutureTask实现了Runnable）
            FutureTask<T> futureTask = new FutureTask<>(task);
            // 装饰FutureTask
            Runnable decorated = taskDecorator.decorate(futureTask);
            // 使用execute执行装饰后的任务
            super.execute(decorated);
            // 返回原始的FutureTask，让调用者能获取结果
            return futureTask;
        }

        @Override
        public CompletableFuture<Void> submitCompletable(Runnable task) {
            Runnable decorate = taskDecorator.decorate(task);
            return super.submitCompletable(decorate);
        }

        @Override
        public <T> CompletableFuture<T> submitCompletable(Callable<T> task) {
            // 创建CompletableFuture
            CompletableFuture<T> future = new CompletableFuture<>();
            // 将Callable包装为Runnable，并在其中完成future
            Runnable decorated = taskDecorator.decorate(() -> {
                try {
                    T result = task.call();
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            // 执行装饰后的任务
            super.execute(decorated);
            return future;
        }

    }

}
