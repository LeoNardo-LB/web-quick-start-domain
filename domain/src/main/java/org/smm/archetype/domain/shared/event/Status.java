package org.smm.archetype.domain.shared.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 事件状态枚举
 *
 * 用于表示领域事件的处理状态
 */
@Getter
@RequiredArgsConstructor
public enum Status {

    /**
     * 事件已发布的状态
     */
    CREATED("已创建"),

    /**
     * 事件正在处理中的状态
     */
    PROCESSING("处理中"),

    /**
     * 消费失败重试的状态
     */
    RETRYING("重试中"),

    /**
     * 消费成功的状态
     */
    SUCCESS("成功"),

    /**
     * 无重试次数后仍然不成功
     */
    FAILED("失败"),

    ;

    /**
     * 状态描述
     */
    private final String description;

}
