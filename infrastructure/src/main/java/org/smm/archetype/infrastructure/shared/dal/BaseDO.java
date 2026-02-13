package org.smm.archetype.infrastructure.shared.dal;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.shared.base.Identifier;

import java.time.Instant;

/**
 * 数据对象基类，自动进行设置操作。
 */
@Getter
@Setter
public abstract class BaseDO implements Identifier {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    private Instant createTime;

    private Instant updateTime;

    private String createUser;

    private String updateUser;

}
