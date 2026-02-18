package org.smm.archetype.infrastructure.shared.dal;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.shared.base.Identifier;

import java.time.Instant;

/**
 * 数据对象基类，自动进行设置操作。
 * 使用 MyBatis-Plus 的自动填充功能处理 createTime、updateTime 等字段。
 */
@Getter
@Setter
public abstract class BaseDO implements Identifier {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Instant createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Instant updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

}
