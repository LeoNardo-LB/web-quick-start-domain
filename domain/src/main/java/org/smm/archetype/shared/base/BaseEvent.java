package org.smm.archetype.shared.base;

import lombok.Getter;
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
     * 事件来源
     */
    protected Source source;

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

    /**
     * 完成事件
     */
    public void complete() {
        this.status = Status.PUBLISHED;
    }

    /**
     * 是否持久化
     */
    public abstract boolean persistent();

    @Getter
    public enum Source {
        ACCESS_WEB
    }

    @Getter
    public enum Type {
        ACCESS
    }

    @Getter
    public enum Status {
        CREATED,
        READY,
        PUBLISHED,
    }

}
