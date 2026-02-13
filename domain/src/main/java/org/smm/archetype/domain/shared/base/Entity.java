package org.smm.archetype.domain.shared.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * 实体基类，提供唯一标识和审计字段。
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public abstract class Entity implements Identifier {

    /**
     * 唯一标识
     */
    protected Long id;

    /**
     * 创建时间
     */
    protected Instant createTime;

    /**
     * 更新时间
     */
    protected Instant updateTime;

    /**
     * 创建人
     */
    protected String createUser;

    /**
     * 更新人
     */
    protected String updateUser;

    /**
     * 版本号（用于乐观锁）
     */
    protected Long version;

    /**
     * 标记为已创建
     *
    在实体创建时调用此方法设置创建时间。
     */
    protected void markAsCreated() {
        if (this.createTime == null) {
            this.createTime = Instant.now();
            this.updateTime = this.createTime;
            this.version = 0L;
        }
    }

    /**
     * 标记为已更新
     *
    在实体的业务方法中修改状态后调用此方法更新时间戳和版本号。
     */
    protected void markAsUpdated() {
        this.updateTime = Instant.now();
        if (this.version != null) {
            this.version++;
        }
    }

}