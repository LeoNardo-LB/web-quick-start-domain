package org.smm.archetype.adapter.access.schedule;

/**
 * 事件重试处理器接口
 *
 * <p>定义事件重试处理的标准接口，用于处理失败事件的重试逻辑。
 * @author Leonardo
 * @since 2026/01/09
 */
public interface EventRetryScheduler {

    /**
     * 调度重试方法
     *
     * <p>定时扫描待处理事件并进行重试。
     */
    void scheduleRetry();

}
