package org.smm.archetype.domain._shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 事件消费状态枚举
 *
 * <p>表示事件在消费者中的处理状态。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum ConsumeStatus {

    /**
     * 准备消费
     * <p>事件已接收，准备执行业务逻辑
     */
    READY("准备消费"),

    /**
     * 已消费
     * <p>事件已成功处理，业务逻辑执行完毕
     */
    CONSUMED("已消费"),

    /**
     * 重试中
     * <p>业务逻辑执行失败，等待重试
     */
    RETRY("重试中"),

    /**
     * 失败
     * <p>重试次数已用尽，事件处理失败
     */
    FAILED("失败");

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 判断是否可以重试
     * @return true-可以重试，false-不可以重试
     */
    public boolean canRetry() {
        return this == RETRY;
    }

    /**
     * 判断是否处理完成（包括成功和失败）
     * @return true-已完成，false-未完成
     */
    public boolean isCompleted() {
        return this == CONSUMED || this == FAILED;
    }

    /**
     * 判断是否处理成功
     * @return true-成功，false-未成功
     */
    public boolean isSuccess() {
        return this == CONSUMED;
    }
}
