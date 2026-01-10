package org.smm.archetype.domain._shared.event;

import lombok.Getter;

/**
 * 事件优先级枚举
 *
 * <p>用于决定事件处理的优先级，高优先级事件会优先被处理。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
public enum EventPriority {

    /**
     * 高优先级
     * <p>适用于关键业务事件，如支付、订单创建等
     * <p>在定时任务中会分配80%的处理资源
     */
    HIGH("高优先级"),

    /**
     * 低优先级
     * <p>适用于非关键业务事件，如日志记录、统计等
     * <p>在定时任务中会分配20%的处理资源
     */
    LOW("低优先级");

    /**
     * 优先级描述
     */
    private final String description;

    /**
     * 构造函数
     * @param description 优先级描述
     */
    EventPriority(String description) {
        this.description = description;
    }

    /**
     * 判断是否为高优先级
     * @return true-高优先级，false-低优先级
     */
    public boolean isHigh() {
        return this == HIGH;
    }

    /**
     * 判断是否为低优先级
     * @return true-低优先级，false-高优先级
     */
    public boolean isLow() {
        return this == LOW;
    }
}
