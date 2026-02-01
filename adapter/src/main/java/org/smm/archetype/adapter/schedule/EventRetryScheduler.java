package org.smm.archetype.adapter.schedule;

/**
 * 事件重试调度接口，处理失败事件的重试逻辑。
 */
public interface EventRetryScheduler {

    /**
     * 调度重试方法。
     */
    void scheduleRetry();

}
