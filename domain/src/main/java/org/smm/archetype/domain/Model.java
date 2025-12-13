package org.smm.archetype.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * @author Leonardo
 * @since 2025/7/14
 * 基础数据对象（Data Object 简称 do）
 */
@Getter
@Setter
public abstract class Model {

    private String id;

    private Instant createTime;

    private Instant updateTime;

    private String createUser;

    private String updateUser;

}