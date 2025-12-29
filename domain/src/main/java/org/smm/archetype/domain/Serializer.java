package org.smm.archetype.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/13
 * 序列模型
 */
@Getter
@Setter
public abstract class Serializer {

    private Long serialno;

    private Instant createTime;

    private String createUser;

}
