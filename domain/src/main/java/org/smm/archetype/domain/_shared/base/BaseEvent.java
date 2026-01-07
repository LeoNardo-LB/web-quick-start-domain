package org.smm.archetype.domain._shared.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/14
 */
@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
public abstract class BaseEvent<T> extends BaseModel {

    /**
     * 前驱事件 id
     */
    protected Long prevId;

    /**
     * 步骤，第几步，若上下文获取不到，则默认为 1
     */
    protected Integer step;

    /**
     * 事件类型
     */
    protected Type type;

    /**
     * 事件载荷
     */
    protected T data;

    /**
     * 事件状态
     */
    protected Status status;

    @Getter
    @RequiredArgsConstructor
    public enum Type {

        NON("空类型事件", 0, false)
        ;

        private final String desc;

        private final int maxRetryCount;

        private final boolean persistent;
    }

    @Getter
    public enum Status {
        /**
         * 已就绪，业务中创建，持久化到库中
         */
        READY,
        /**
         * 已发布
         */
        PUBLISHED,
    }

}
