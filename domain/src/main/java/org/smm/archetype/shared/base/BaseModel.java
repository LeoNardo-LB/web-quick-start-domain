package org.smm.archetype.shared.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * @author Leonardo
 * @since 2025/7/14
 * 基础数据对象（Data Object 简称 do）
 */
@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
public abstract class BaseModel implements Identifier {

    protected Long id;

    protected Instant createTime;

    protected Instant updateTime;

    protected String createUser;

    protected String updateUser;

}