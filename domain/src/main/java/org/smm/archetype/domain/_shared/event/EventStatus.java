package org.smm.archetype.domain._shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 事件发布状态枚举
 *
 * <p>表示事件在发布过程中的状态流转。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum EventStatus {

    /**
     * 已创建
     * <p>事件已创建但未准备发布
     */
    CREATED("已创建"),

    /**
     * 就绪
     * <p>事件已准备好，等待发布到消息队列
     */
    READY("就绪"),

    /**
     * 已发布
     * <p>事件已成功发布到消息队列
     */
    PUBLISHED("已发布");

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 判断是否可以发布
     * @return true-可以发布，false-不可以发布
     */
    public boolean canPublish() {
        return this == READY;
    }

    /**
     * 判断是否已发布
     * @return true-已发布，false-未发布
     */
    public boolean isPublished() {
        return this == PUBLISHED;
    }
}
