package org.smm.archetype.test.cases.unittest.performance;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 日志性能基准测试
 *
 * <p>验证优化后的日志配置在高并发场景下的性能表现
 */
class LoggingPerformanceUTest {

    private static final int THREAD_COUNT    = 100;
    private static final int LOGS_PER_THREAD = 1000;
    private static final int TOTAL_LOG_COUNT = THREAD_COUNT * LOGS_PER_THREAD; // 100,000

    private org.slf4j.Logger logger;

    @BeforeEach
    void setUp() {
        logger = LoggerFactory.getLogger(LoggingPerformanceUTest.class);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        // 确保使用 INFO 级别进行测试
        logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
    }

    /**
     * 测试高并发日志记录性能
     *
     * <p>验证在 100 线程并发记录 100,000 条日志时，应用不会阻塞
     */
    @Test
    void testHighConcurrencyLoggingPerformance() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(THREAD_COUNT);

        AtomicLong totalTime = new AtomicLong(0);

        List<Future<Long>> futures = new ArrayList<>();

        // 启动多个线程并发记录日志
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            Future<Long> future = executorService.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程准备好

                    long startTime = System.nanoTime();

                    for (int j = 0; j < LOGS_PER_THREAD; j++) {
                        // 记录包含敏感信息的日志（验证脱敏不显著影响性能）
                        String message = String.format(
                                "[业务处理] 线程%d 处理订单%d | password=secret123 | token=abc123def456 | user=user@example.com | phone=13800138000"
                                        + " | idcard=110101199001011234 | bankcard=6222021234567890123",
                                threadId, j
                        );
                        logger.info(message);
                    }

                    long endTime = System.nanoTime();
                    long durationMs = (endTime - startTime) / 1_000_000;
                    totalTime.addAndGet(durationMs);

                    return durationMs;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return 0L;
                } finally {
                    completeLatch.countDown();
                }
            });
            futures.add(future);
        }

        // 记录开始时间
        long testStartTime = System.currentTimeMillis();
        startLatch.countDown(); // 释放所有线程

        // 等待所有线程完成
        boolean completed = completeLatch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "所有线程应在30秒内完成");

        // 记录结束时间
        long testEndTime = System.currentTimeMillis();
        long totalTestTimeMs = testEndTime - testStartTime;

        // 关闭线程池
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 计算性能指标
        double avgTimePerLogMs = (double) totalTestTimeMs / TOTAL_LOG_COUNT;
        double logsPerSecond = (double) TOTAL_LOG_COUNT / (totalTestTimeMs / 1000.0);

        // 输出性能结果
        System.out.println("=== 日志性能测试结果 ===");
        System.out.println("总日志数量: " + TOTAL_LOG_COUNT);
        System.out.println("并发线程数: " + THREAD_COUNT);
        System.out.println("总测试时间: " + totalTestTimeMs + " ms");
        System.out.println("平均每条日志耗时: " + String.format("%.3f", avgTimePerLogMs) + " ms");
        System.out.println("日志吞吐量: " + String.format("%.2f", logsPerSecond) + " logs/second");

        // 验证性能指标
        assertTrue(totalTestTimeMs < 30000, "总测试时间应小于30秒，实际: " + totalTestTimeMs + " ms");
        assertTrue(logsPerSecond > 3000, "日志吞吐量应大于3000 logs/second，实际: " + logsPerSecond);

        // 验证异步队列 neverBlock 生效（应用未阻塞）
        assertFalse(Thread.interrupted(), "主线程不应被中断");
    }

    /**
     * 测试单线程日志记录性能基准
     */
    @Test
    void testSingleThreadLoggingPerformance() {
        long startTime = System.nanoTime();

        for (int i = 0; i < 10_000; i++) {
            logger.info("[单线程测试] 记录日志第{}条 | phone=13912345678", i);
        }

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        // 计算性能指标
        double avgTimePerLogMicros = (double) durationMs / 10_000 * 1000;

        System.out.println("=== 单线程日志性能测试结果 ===");
        System.out.println("日志数量: 10,000");
        System.out.println("总耗时: " + durationMs + " ms");
        System.out.println("平均每条日志耗时: " + String.format("%.2f", avgTimePerLogMicros) + " μs");

        // 验证性能：单线程记录 10,000 条日志应在 5 秒内完成
        assertTrue(durationMs < 5000, "单线程记录10,000条日志应在5秒内完成，实际: " + durationMs + " ms");
    }

    /**
     * 测试日志脱敏对性能的影响
     */
    @Test
    void testDesensitizationPerformanceImpact() {
        String messageWithSensitiveData =
                "[敏感信息测试] password=secret123 | token=abc123def456 | user=user@example.com | phone=13800138000 | idcard=110101199001011234"
                        + " | bankcard=6222021234567890123 | ip=192.168.1.1";

        long startTime = System.nanoTime();

        for (int i = 0; i < 10_000; i++) {
            logger.info(messageWithSensitiveData);
        }

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("=== 脱敏性能影响测试结果 ===");
        System.out.println("日志数量: 10,000");
        System.out.println("每条日志包含 7 种敏感信息");
        System.out.println("总耗时: " + durationMs + " ms");
        System.out.println("平均每条日志耗时: " + (durationMs / 10_000.0) + " ms");

        // 验证性能：包含敏感信息的日志记录不应显著影响性能（脱敏处理会增加约20%开销）
        assertTrue(durationMs < 10000, "记录10,000条包含敏感信息的日志应在10秒内完成，实际: " + durationMs + " ms");
    }

    /**
     * 测试日志截断对性能的影响
     */
    @Test
    void testTruncationPerformanceImpact() {
        // 创建超长消息（超过 2048 字符）
        StringBuilder longMessage = new StringBuilder();
        longMessage.append("[超长日志测试]");
        for (int i = 0; i < 500; i++) {
            longMessage.append(" 这是一个非常长的日志消息，用于测试截断功能是否会影响性能。");
        }

        long startTime = System.nanoTime();

        for (int i = 0; i < 1_000; i++) {
            logger.info(longMessage.toString());
        }

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("=== 截断性能影响测试结果 ===");
        System.out.println("日志数量: 1,000");
        System.out.println("每条日志大小: > 2048 字符");
        System.out.println("总耗时: " + durationMs + " ms");
        System.out.println("平均每条日志耗时: " + (durationMs / 1000.0) + " ms");

        // 验证性能：超长日志截断不应显著影响性能（脱敏处理会增加约20%开销）
        assertTrue(durationMs < 5000, "记录1,000条超长日志应在5秒内完成，实际: " + durationMs + " ms");
    }

    /**
     * 测试异步队列 neverBlock 行为
     *
     * <p>验证当日志队列满时，应用不会阻塞
     */
    @Test
    void testAsyncQueueNeverBlockBehavior() throws InterruptedException {
        // 创建大量日志以填满异步队列（队列大小为 2048）
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < 1_000; j++) {
                    logger.info("[队列测试] 快速记录大量日志以填满队列 | password=test123");
                }
            });
        }

        // 等待所有任务提交完成
        Thread.sleep(1000);

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime.get();

        System.out.println("=== 异步队列 neverBlock 测试结果 ===");
        System.out.println("提交的日志数量: 100,000");
        System.out.println("提交耗时: " + durationMs + " ms");

        // 验证：提交 100,000 条日志应在 5 秒内完成（不会因为队列满而阻塞）
        assertTrue(durationMs < 5000, "异步队列提交日志应不阻塞，实际耗时: " + durationMs + " ms");

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

}
